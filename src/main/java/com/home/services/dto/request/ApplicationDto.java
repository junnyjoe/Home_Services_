package com.home.services.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour créer une candidature
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDto {

    @NotNull(message = "L'ID de l'annonce est obligatoire")
    private Long serviceRequestId;

    @Size(max = 1000, message = "Le message ne peut pas dépasser 1000 caractères")
    private String message;

    private BigDecimal proposedPrice;

    private Integer proposedDays;
}
