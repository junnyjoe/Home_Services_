package com.home.services.dto.request;

import com.home.services.model.enums.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour créer ou modifier une annonce
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDto {

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 20, max = 2000, message = "La description doit contenir entre 20 et 2000 caractères")
    private String description;

    @NotBlank(message = "Le quartier est obligatoire")
    private String quartier;

    private String adresse;

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    private LocalDate datePrestation;

    private Urgency urgence;

    // true = publier directement, false = sauvegarder en brouillon
    @Builder.Default
    private Boolean publier = true;
}
