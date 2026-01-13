package com.home.services.repository;

import com.home.services.model.Application;
import com.home.services.model.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Candidatures d'un prestataire
    List<Application> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<Application> findByProviderIdAndStatut(Long providerId, ApplicationStatus statut);

    // Candidatures pour une annonce
    List<Application> findByServiceRequestIdOrderByCreatedAtDesc(Long serviceRequestId);

    List<Application> findByServiceRequestIdAndStatut(Long serviceRequestId, ApplicationStatus statut);

    // Vérifier si un prestataire a déjà postulé
    boolean existsByServiceRequestIdAndProviderId(Long serviceRequestId, Long providerId);

    // Trouver une candidature spécifique
    Optional<Application> findByServiceRequestIdAndProviderId(Long serviceRequestId, Long providerId);

    // Compter les candidatures par statut pour un prestataire
    long countByProviderIdAndStatut(Long providerId, ApplicationStatus statut);

    // Compter les candidatures pour une annonce
    long countByServiceRequestId(Long serviceRequestId);

    // Candidatures en attente pour les annonces d'un client
    @Query("SELECT a FROM Application a WHERE a.serviceRequest.client.id = :clientId " +
            "AND a.statut = 'EN_ATTENTE' ORDER BY a.createdAt DESC")
    List<Application> findPendingForClient(@Param("clientId") Long clientId);
}
