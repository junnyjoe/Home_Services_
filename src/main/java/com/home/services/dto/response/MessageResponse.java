package com.home.services.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour afficher un message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private Long applicationId;

    // Expéditeur
    private Long senderId;
    private String senderNom;
    private Boolean isOwnMessage; // true si l'utilisateur courant est l'expéditeur

    private String content;
    private Boolean isRead;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
