package com.home.services.repository;

import com.home.services.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Avis d'un prestataire
    List<Review> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    // Vérifier si un avis existe pour une application
    boolean existsByApplicationId(Long applicationId);

    Optional<Review> findByApplicationId(Long applicationId);

    // Moyenne des notes d'un prestataire
    @Query("SELECT AVG(r.note) FROM Review r WHERE r.provider.id = :providerId")
    Double findAverageNoteByProviderId(@Param("providerId") Long providerId);

    // Compter les avis d'un prestataire
    long countByProviderId(Long providerId);

    // Avis récents (pour admin)
    List<Review> findTop20ByOrderByCreatedAtDesc();
}
