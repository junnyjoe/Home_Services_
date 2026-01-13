package com.home.services.service;

import com.home.services.dto.request.ServiceRequestDto;
import com.home.services.dto.response.ServiceRequestResponse;
import com.home.services.exception.ResourceNotFoundException;
import com.home.services.model.Category;
import com.home.services.model.ServiceRequest;
import com.home.services.model.User;
import com.home.services.model.enums.RequestStatus;
import com.home.services.model.enums.Urgency;
import com.home.services.repository.CategoryRepository;
import com.home.services.repository.ServiceRequestRepository;
import com.home.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des annonces de services
 */
@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Créer une nouvelle annonce
     */
    @Transactional
    public ServiceRequestResponse create(ServiceRequestDto dto, String userEmail) {
        User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", userEmail));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", dto.getCategoryId()));

        ServiceRequest request = ServiceRequest.builder()
                .client(client)
                .category(category)
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .quartier(dto.getQuartier())
                .adresse(dto.getAdresse())
                .budgetMin(dto.getBudgetMin())
                .budgetMax(dto.getBudgetMax())
                .datePrestation(dto.getDatePrestation())
                .urgence(dto.getUrgence() != null ? dto.getUrgence() : Urgency.NORMAL)
                .statut(Boolean.TRUE.equals(dto.getPublier()) ? RequestStatus.PUBLIEE : RequestStatus.BROUILLON)
                .nombreCandidatures(0)
                .build();

        request = serviceRequestRepository.save(request);
        return toResponse(request, true);
    }

    /**
     * Mettre à jour une annonce
     */
    @Transactional
    public ServiceRequestResponse update(Long id, ServiceRequestDto dto, String userEmail) {
        ServiceRequest request = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        // Vérifier que l'utilisateur est le propriétaire
        if (!request.getClient().getEmail().equals(userEmail)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        // Vérifier que l'annonce peut être modifiée
        if (request.getStatut() == RequestStatus.EN_COURS || request.getStatut() == RequestStatus.TERMINEE) {
            throw new RuntimeException("Cette annonce ne peut plus être modifiée");
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", dto.getCategoryId()));
            request.setCategory(category);
        }

        if (dto.getTitre() != null)
            request.setTitre(dto.getTitre());
        if (dto.getDescription() != null)
            request.setDescription(dto.getDescription());
        if (dto.getQuartier() != null)
            request.setQuartier(dto.getQuartier());
        if (dto.getAdresse() != null)
            request.setAdresse(dto.getAdresse());
        if (dto.getBudgetMin() != null)
            request.setBudgetMin(dto.getBudgetMin());
        if (dto.getBudgetMax() != null)
            request.setBudgetMax(dto.getBudgetMax());
        if (dto.getDatePrestation() != null)
            request.setDatePrestation(dto.getDatePrestation());
        if (dto.getUrgence() != null)
            request.setUrgence(dto.getUrgence());

        // Publier si demandé
        if (Boolean.TRUE.equals(dto.getPublier()) && request.getStatut() == RequestStatus.BROUILLON) {
            request.setStatut(RequestStatus.PUBLIEE);
        }

        request = serviceRequestRepository.save(request);
        return toResponse(request, true);
    }

    /**
     * Supprimer/Annuler une annonce
     */
    @Transactional
    public void delete(Long id, String userEmail) {
        ServiceRequest request = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        if (!request.getClient().getEmail().equals(userEmail)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette annonce");
        }

        if (request.getStatut() == RequestStatus.EN_COURS) {
            throw new RuntimeException("Impossible d'annuler une annonce en cours");
        }

        request.setStatut(RequestStatus.ANNULEE);
        serviceRequestRepository.save(request);
    }

    /**
     * Obtenir une annonce par ID
     */
    public ServiceRequestResponse getById(Long id, String userEmail) {
        ServiceRequest request = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        // L'adresse n'est visible que par le propriétaire ou le prestataire sélectionné
        boolean showAddress = request.getClient().getEmail().equals(userEmail) ||
                (request.getSelectedProvider() != null && request.getSelectedProvider().getEmail().equals(userEmail));

        return toResponse(request, showAddress);
    }

    /**
     * Lister les annonces d'un client
     */
    public List<ServiceRequestResponse> getByClient(String userEmail) {
        User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", userEmail));

        return serviceRequestRepository.findByClientIdOrderByCreatedAtDesc(client.getId())
                .stream()
                .map(r -> toResponse(r, true))
                .collect(Collectors.toList());
    }

    /**
     * Lister les annonces publiées (pour prestataires)
     */
    public List<ServiceRequestResponse> getPublished(Long categoryId, String quartier) {
        List<ServiceRequest> requests;

        if (categoryId != null || quartier != null) {
            requests = serviceRequestRepository.findByFilters(RequestStatus.PUBLIEE, categoryId, quartier);
        } else {
            requests = serviceRequestRepository.findByStatutOrderByCreatedAtDesc(RequestStatus.PUBLIEE);
        }

        return requests.stream()
                .map(r -> toResponse(r, false)) // Pas d'adresse pour les non-propriétaires
                .collect(Collectors.toList());
    }

    /**
     * Rechercher par mot-clé
     */
    public List<ServiceRequestResponse> search(String keyword) {
        return serviceRequestRepository.searchByKeyword(keyword)
                .stream()
                .map(r -> toResponse(r, false))
                .collect(Collectors.toList());
    }

    /**
     * Convertir en DTO de réponse
     */
    private ServiceRequestResponse toResponse(ServiceRequest request, boolean showAddress) {
        return ServiceRequestResponse.builder()
                .id(request.getId())
                .clientId(request.getClient().getId())
                .clientNom(request.getClient().getNom())
                .categoryId(request.getCategory().getId())
                .categoryNom(request.getCategory().getNom())
                .categoryIcone(request.getCategory().getIcone())
                .titre(request.getTitre())
                .description(request.getDescription())
                .quartier(request.getQuartier())
                .adresse(showAddress ? request.getAdresse() : null)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .datePrestation(request.getDatePrestation())
                .urgence(request.getUrgence())
                .statut(request.getStatut())
                .nombreCandidatures(request.getNombreCandidatures())
                .selectedProviderId(
                        request.getSelectedProvider() != null ? request.getSelectedProvider().getId() : null)
                .selectedProviderNom(
                        request.getSelectedProvider() != null ? request.getSelectedProvider().getNom() : null)
                .createdAt(request.getCreatedAt())
                .expiresAt(request.getExpiresAt())
                .build();
    }
}
