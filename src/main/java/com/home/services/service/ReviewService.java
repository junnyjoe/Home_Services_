package com.home.services.service;

import com.home.services.dto.request.ReviewDto;
import com.home.services.dto.response.ReviewResponse;
import com.home.services.exception.ResourceNotFoundException;
import com.home.services.model.Application;
import com.home.services.model.ProviderProfile;
import com.home.services.model.Review;
import com.home.services.model.User;
import com.home.services.model.enums.ApplicationStatus;
import com.home.services.model.enums.RequestStatus;
import com.home.services.repository.ApplicationRepository;
import com.home.services.repository.ProviderProfileRepository;
import com.home.services.repository.ReviewRepository;
import com.home.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des avis
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;

    /**
     * Laisser un avis
     */
    @Transactional
    public ReviewResponse create(ReviewDto dto, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", clientEmail));

        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Prestation", "id", dto.getApplicationId()));

        // Vérifier que c'est le bon client
        if (!application.getServiceRequest().getClient().getId().equals(client.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à laisser un avis pour cette prestation");
        }

        // Vérifier que la prestation est terminée
        if (application.getServiceRequest().getStatut() != RequestStatus.TERMINEE) {
            throw new RuntimeException("Vous ne pouvez laisser un avis qu'après la fin de la prestation");
        }

        // Vérifier qu'il n'y a pas déjà un avis
        if (reviewRepository.existsByApplicationId(application.getId())) {
            throw new RuntimeException("Vous avez déjà laissé un avis pour cette prestation");
        }

        User provider = application.getProvider();

        Review review = Review.builder()
                .application(application)
                .client(client)
                .provider(provider)
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .noteQualite(dto.getNoteQualite())
                .notePonctualite(dto.getNotePonctualite())
                .noteCommunication(dto.getNoteCommunication())
                .build();

        review = reviewRepository.save(review);

        // Mettre à jour la note moyenne du prestataire
        updateProviderRating(provider.getId());

        return toResponse(review);
    }

    /**
     * Récupérer les avis d'un prestataire
     */
    public List<ReviewResponse> getByProvider(Long providerId) {
        return reviewRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer la note moyenne d'un prestataire
     */
    public Double getAverageRating(Long providerId) {
        return reviewRepository.findAverageNoteByProviderId(providerId);
    }

    /**
     * Mettre à jour la note globale du prestataire
     */
    private void updateProviderRating(Long providerId) {
        Double avgRating = reviewRepository.findAverageNoteByProviderId(providerId);
        long count = reviewRepository.countByProviderId(providerId);

        if (avgRating != null) {
            ProviderProfile profile = providerProfileRepository.findByUserId(providerId)
                    .orElse(null);

            if (profile != null) {
                profile.setNoteGlobale(avgRating);
                profile.setNombreAvis((int) count);
                providerProfileRepository.save(profile);
            }
        }
    }

    /**
     * Récupérer les avis récents (admin)
     */
    public List<ReviewResponse> getRecent() {
        return reviewRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .applicationId(review.getApplication().getId())
                .requestTitre(review.getApplication().getServiceRequest().getTitre())
                .clientId(review.getClient().getId())
                .clientNom(review.getClient().getNom())
                .providerId(review.getProvider().getId())
                .providerNom(review.getProvider().getNom())
                .note(review.getNote())
                .commentaire(review.getCommentaire())
                .noteQualite(review.getNoteQualite())
                .notePonctualite(review.getNotePonctualite())
                .noteCommunication(review.getNoteCommunication())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
