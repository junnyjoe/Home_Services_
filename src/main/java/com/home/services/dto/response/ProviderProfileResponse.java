package com.home.services.dto.response;

import com.home.services.model.enums.ProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pour afficher le profil d'un prestataire
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfileResponse {

    private Long id;
    private Long userId;
    private String nom;
    private String email;
    private String telephone;
    private String bio;
    private List<String> competences;
    private String quartier;
    private BigDecimal tarifHoraire;
    private BigDecimal noteGlobale;
    private Integer nombreAvis;
    private ProfileStatus statut;
    private List<CategoryResponse> categories;
}
