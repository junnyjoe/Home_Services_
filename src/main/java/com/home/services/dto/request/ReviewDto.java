package com.home.services.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer un avis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    @NotNull(message = "L'ID de la prestation est obligatoire")
    private Long applicationId;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    private Integer note;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String commentaire;

    @Min(1)
    @Max(5)
    private Integer noteQualite;

    @Min(1)
    @Max(5)
    private Integer notePonctualite;

    @Min(1)
    @Max(5)
    private Integer noteCommunication;
}
