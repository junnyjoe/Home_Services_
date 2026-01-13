package com.home.services.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour afficher un avis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;

    // Info prestation
    private Long applicationId;
    private String requestTitre;

    // Client qui a donné l'avis
    private Long clientId;
    private String clientNom;

    // Prestataire évalué
    private Long providerId;
    private String providerNom;

    // Notes
    private Integer note;
    private String commentaire;
    private Integer noteQualite;
    private Integer notePonctualite;
    private Integer noteCommunication;

    private LocalDateTime createdAt;
}
