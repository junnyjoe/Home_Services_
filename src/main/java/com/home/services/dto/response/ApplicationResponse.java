package com.home.services.dto.response;

import com.home.services.model.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour afficher une candidature
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {

    private Long id;

    // Info annonce
    private Long serviceRequestId;
    private String serviceRequestTitre;
    private String serviceRequestQuartier;

    // Info prestataire
    private Long providerId;
    private String providerNom;
    private String providerTelephone; // Visible après acceptation
    private Double providerNote;

    // Détails candidature
    private String message;
    private BigDecimal proposedPrice;
    private Integer proposedDays;

    private ApplicationStatus statut;
    private String clientResponse;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
