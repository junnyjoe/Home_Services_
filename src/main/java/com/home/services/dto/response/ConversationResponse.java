package com.home.services.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour afficher une conversation (résumé)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {

    private Long applicationId;

    // Détails annonce
    private String requestTitre;

    // L'autre participant
    private Long otherUserId;
    private String otherUserNom;
    private String otherUserRole; // "Client" ou "Prestataire"
    private String otherUserPhone; // Téléphone (visible après match)

    // Dernier message
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Boolean hasUnread;
}
