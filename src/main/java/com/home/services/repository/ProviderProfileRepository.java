package com.home.services.repository;

import com.home.services.model.ProviderProfile;
import com.home.services.model.enums.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {

    Optional<ProviderProfile> findByUserId(Long userId);

    List<ProviderProfile> findByStatut(ProfileStatus statut);

    List<ProviderProfile> findByStatutAndQuartier(ProfileStatus statut, String quartier);

    @Query("SELECT p FROM ProviderProfile p JOIN p.categories c WHERE c.id = :categoryId AND p.statut = 'VERIFIE'")
    List<ProviderProfile> findVerifiedByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM ProviderProfile p JOIN p.categories c WHERE c.id = :categoryId AND p.quartier = :quartier AND p.statut = 'VERIFIE'")
    List<ProviderProfile> findVerifiedByCategoryAndQuartier(@Param("categoryId") Long categoryId,
            @Param("quartier") String quartier);

    List<ProviderProfile> findByStatutOrderByNoteGlobaleDesc(ProfileStatus statut);
}
