package com.home.services.dto.response;

import com.home.services.model.enums.RequestStatus;
import com.home.services.model.enums.Urgency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour afficher une annonce
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponse {

    private Long id;

    // Info client
    private Long clientId;
    private String clientNom;

    // Info catégorie
    private Long categoryId;
    private String categoryNom;
    private String categoryIcone;

    // Détails annonce
    private String titre;
    private String description;
    private String quartier;
    private String adresse; // null si non autorisé

    private BigDecimal budgetMin;
    private BigDecimal budgetMax;

    private LocalDate datePrestation;
    private Urgency urgence;
    private RequestStatus statut;

    private Integer nombreCandidatures;

    // Prestataire sélectionné (si applicable)
    private Long selectedProviderId;
    private String selectedProviderNom;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
