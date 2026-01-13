package com.home.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour envoyer un message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    @NotNull(message = "L'ID de la conversation est obligatoire")
    private Long applicationId;

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(max = 2000, message = "Le message ne peut pas dépasser 2000 caractères")
    private String content;
}
