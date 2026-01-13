package com.home.services.dto.response;

import com.home.services.model.enums.DocumentStatus;
import com.home.services.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour afficher un document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;

    private Long providerId;
    private String providerNom;

    private DocumentType type;
    private String typeLabel;

    private String filename;
    private String url;

    private DocumentStatus statut;
    private String statutLabel;
    private String motifRefus;

    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
}
