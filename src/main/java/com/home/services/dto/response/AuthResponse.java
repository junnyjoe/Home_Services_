package com.home.services.dto.response;

import com.home.services.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'authentification (login/register)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String nom;
    private String email;
    private Role role;
    private Boolean verified;

    public AuthResponse(String accessToken, Long userId, String nom, String email, Role role, Boolean verified) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.verified = verified;
    }
}
