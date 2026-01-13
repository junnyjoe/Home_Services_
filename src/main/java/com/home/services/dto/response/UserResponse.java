package com.home.services.dto.response;

import com.home.services.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour afficher les informations d'un utilisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private Role role;
    private Boolean verified;
    private Boolean active;
    private LocalDateTime createdAt;

    // Infos profil prestataire (si applicable)
    private ProviderProfileResponse providerProfile;
}
