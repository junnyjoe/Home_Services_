package com.home.services.service;

import com.home.services.dto.request.ApplicationDto;
import com.home.services.dto.response.ApplicationResponse;
import com.home.services.exception.ResourceNotFoundException;
import com.home.services.model.Application;
import com.home.services.model.ServiceRequest;
import com.home.services.model.User;
import com.home.services.model.enums.ApplicationStatus;
import com.home.services.model.enums.RequestStatus;
import com.home.services.model.enums.Role;
import com.home.services.repository.ApplicationRepository;
import com.home.services.repository.ServiceRequestRepository;
import com.home.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des candidatures
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    /**
     * Postuler à une annonce
     */
    @Transactional
    public ApplicationResponse apply(ApplicationDto dto, String providerEmail) {
        User provider = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", providerEmail));

        // Vérifier que c'est un prestataire
        if (provider.getRole() != Role.PRESTATAIRE) {
            throw new RuntimeException("Seuls les prestataires peuvent postuler");
        }

        ServiceRequest request = serviceRequestRepository.findById(dto.getServiceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", dto.getServiceRequestId()));

        // Vérifier que l'annonce est publiée
        if (request.getStatut() != RequestStatus.PUBLIEE) {
            throw new RuntimeException("Cette annonce n'accepte plus de candidatures");
        }

        // Vérifier que le prestataire n'a pas déjà postulé
        if (applicationRepository.existsByServiceRequestIdAndProviderId(request.getId(), provider.getId())) {
            throw new RuntimeException("Vous avez déjà postulé à cette annonce");
        }

        Application application = Application.builder()
                .serviceRequest(request)
                .provider(provider)
                .message(dto.getMessage())
                .proposedPrice(dto.getProposedPrice())
                .proposedDays(dto.getProposedDays())
                .statut(ApplicationStatus.EN_ATTENTE)
                .build();

        application = applicationRepository.save(application);

        // Incrémenter le compteur de candidatures
        request.setNombreCandidatures(request.getNombreCandidatures() + 1);
        serviceRequestRepository.save(request);

        return toResponse(application, false);
    }

    /**
     * Retirer une candidature
     */
    @Transactional
    public void withdraw(Long applicationId, String providerEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", "id", applicationId));

        if (!application.getProvider().getEmail().equals(providerEmail)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à retirer cette candidature");
        }

        if (application.getStatut() != ApplicationStatus.EN_ATTENTE) {
            throw new RuntimeException("Cette candidature ne peut plus être retirée");
        }

        application.setStatut(ApplicationStatus.RETIREE);
        applicationRepository.save(application);

        // Décrémenter le compteur
        ServiceRequest request = application.getServiceRequest();
        request.setNombreCandidatures(Math.max(0, request.getNombreCandidatures() - 1));
        serviceRequestRepository.save(request);
    }

    /**
     * Accepter une candidature (Client)
     */
    @Transactional
    public ApplicationResponse accept(Long applicationId, String clientEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", "id", applicationId));

        ServiceRequest request = application.getServiceRequest();

        if (!request.getClient().getEmail().equals(clientEmail)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à gérer cette candidature");
        }

        if (application.getStatut() != ApplicationStatus.EN_ATTENTE) {
            throw new RuntimeException("Cette candidature a déjà été traitée");
        }

        // Accepter cette candidature
        application.setStatut(ApplicationStatus.ACCEPTEE);
        application.setRespondedAt(LocalDateTime.now());
        applicationRepository.save(application);

        // Mettre à jour l'annonce
        request.setStatut(RequestStatus.EN_COURS);
        request.setSelectedProvider(application.getProvider());
        serviceRequestRepository.save(request);

        // Refuser automatiquement les autres candidatures en attente
        List<Application> otherApplications = applicationRepository
                .findByServiceRequestIdAndStatut(request.getId(), ApplicationStatus.EN_ATTENTE);
        for (Application other : otherApplications) {
            if (!other.getId().equals(applicationId)) {
                other.setStatut(ApplicationStatus.REFUSEE);
                other.setClientResponse("Un autre prestataire a été sélectionné");
                other.setRespondedAt(LocalDateTime.now());
                applicationRepository.save(other);
            }
        }

        return toResponse(application, true);
    }

    /**
     * Refuser une candidature (Client)
     */
    @Transactional
    public ApplicationResponse reject(Long applicationId, String clientEmail, String reason) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", "id", applicationId));

        ServiceRequest request = application.getServiceRequest();

        if (!request.getClient().getEmail().equals(clientEmail)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à gérer cette candidature");
        }

        if (application.getStatut() != ApplicationStatus.EN_ATTENTE) {
            throw new RuntimeException("Cette candidature a déjà été traitée");
        }

        application.setStatut(ApplicationStatus.REFUSEE);
        application.setClientResponse(reason);
        application.setRespondedAt(LocalDateTime.now());
        applicationRepository.save(application);

        return toResponse(application, false);
    }

    /**
     * Lister les candidatures d'un prestataire
     */
    public List<ApplicationResponse> getByProvider(String providerEmail) {
        User provider = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", providerEmail));

        return applicationRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(a -> toResponse(a, a.getStatut() == ApplicationStatus.ACCEPTEE))
                .collect(Collectors.toList());
    }

    /**
     * Lister les candidatures pour une annonce (Client)
     */
    public List<ApplicationResponse> getByServiceRequest(Long serviceRequestId, String clientEmail) {
        ServiceRequest request = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", serviceRequestId));

        if (!request.getClient().getEmail().equals(clientEmail)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à voir ces candidatures");
        }

        return applicationRepository.findByServiceRequestIdOrderByCreatedAtDesc(serviceRequestId)
                .stream()
                .map(a -> toResponse(a, request.getStatut() == RequestStatus.EN_COURS))
                .collect(Collectors.toList());
    }

    /**
     * Convertir en DTO de réponse
     */
    private ApplicationResponse toResponse(Application application, boolean showContact) {
        User provider = application.getProvider();
        Double rating = null;
        if (provider.getProviderProfile() != null) {
            rating = provider.getProviderProfile().getNoteGlobale();
        }

        return ApplicationResponse.builder()
                .id(application.getId())
                .serviceRequestId(application.getServiceRequest().getId())
                .serviceRequestTitre(application.getServiceRequest().getTitre())
                .serviceRequestQuartier(application.getServiceRequest().getQuartier())
                .providerId(provider.getId())
                .providerNom(provider.getNom())
                .providerTelephone(showContact ? provider.getTelephone() : null)
                .providerNote(rating)
                .message(application.getMessage())
                .proposedPrice(application.getProposedPrice())
                .proposedDays(application.getProposedDays())
                .statut(application.getStatut())
                .clientResponse(application.getClientResponse())
                .createdAt(application.getCreatedAt())
                .respondedAt(application.getRespondedAt())
                .build();
    }
}
