package com.home.services.controller;

import com.home.services.dto.request.MessageDto;
import com.home.services.dto.response.ApiResponse;
import com.home.services.dto.response.ConversationResponse;
import com.home.services.dto.response.MessageResponse;
import com.home.services.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la messagerie
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Envoyer un message
     * POST /api/messages
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> send(
            @Valid @RequestBody MessageDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageResponse response = messageService.send(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Message envoyé", response));
    }

    /**
     * Récupérer les messages d'une conversation
     * GET /api/messages/conversation/{applicationId}
     */
    @GetMapping("/conversation/{applicationId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversation(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MessageResponse> responses = messageService.getConversation(applicationId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lister les conversations
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ConversationResponse> responses = messageService.getConversations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Compter les messages non lus
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
            @AuthenticationPrincipal UserDetails userDetails) {

        long count = messageService.countUnread(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }
}
