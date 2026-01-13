package com.home.services.repository;

import com.home.services.model.ServiceRequest;
import com.home.services.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    // Annonces d'un client
    List<ServiceRequest> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<ServiceRequest> findByClientIdAndStatut(Long clientId, RequestStatus statut);

    // Annonces publiées (pour prestataires)
    List<ServiceRequest> findByStatutOrderByCreatedAtDesc(RequestStatus statut);

    // Filtrage par catégorie
    List<ServiceRequest> findByStatutAndCategoryIdOrderByCreatedAtDesc(RequestStatus statut, Long categoryId);

    // Filtrage par quartier
    List<ServiceRequest> findByStatutAndQuartierOrderByCreatedAtDesc(RequestStatus statut, String quartier);

    // Filtrage par catégorie et quartier
    List<ServiceRequest> findByStatutAndCategoryIdAndQuartierOrderByCreatedAtDesc(
            RequestStatus statut, Long categoryId, String quartier);

    // Recherche combinée avec requête personnalisée
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.statut = :statut " +
            "AND (:categoryId IS NULL OR sr.category.id = :categoryId) " +
            "AND (:quartier IS NULL OR sr.quartier = :quartier) " +
            "ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findByFilters(
            @Param("statut") RequestStatus statut,
            @Param("categoryId") Long categoryId,
            @Param("quartier") String quartier);

    // Compter les annonces par statut pour un client
    long countByClientIdAndStatut(Long clientId, RequestStatus statut);

    // Recherche par mot-clé dans titre ou description
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.statut = 'PUBLIEE' " +
            "AND (LOWER(sr.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(sr.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY sr.createdAt DESC")
    List<ServiceRequest> searchByKeyword(@Param("keyword") String keyword);
}
