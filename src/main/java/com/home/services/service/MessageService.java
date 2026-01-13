package com.home.services.service;

import com.home.services.dto.request.MessageDto;
import com.home.services.dto.response.ConversationResponse;
import com.home.services.dto.response.MessageResponse;
import com.home.services.exception.ResourceNotFoundException;
import com.home.services.model.Application;
import com.home.services.model.Message;
import com.home.services.model.User;
import com.home.services.model.enums.ApplicationStatus;
import com.home.services.repository.ApplicationRepository;
import com.home.services.repository.MessageRepository;
import com.home.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des messages
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    /**
     * Envoyer un message
     */
    @Transactional
    public MessageResponse send(MessageDto dto, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", senderEmail));

        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", dto.getApplicationId()));

        // Vérifier que l'application est acceptée
        if (application.getStatut() != ApplicationStatus.ACCEPTEE) {
            throw new RuntimeException("Vous ne pouvez pas envoyer de messages pour cette conversation");
        }

        // Vérifier que l'utilisateur fait partie de la conversation
        Long clientId = application.getServiceRequest().getClient().getId();
        Long providerId = application.getProvider().getId();

        if (!sender.getId().equals(clientId) && !sender.getId().equals(providerId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à envoyer des messages dans cette conversation");
        }

        Message message = Message.builder()
                .application(application)
                .sender(sender)
                .content(dto.getContent())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        return toMessageResponse(message, sender.getId());
    }

    /**
     * Récupérer les messages d'une conversation
     */
    @Transactional
    public List<MessageResponse> getConversation(Long applicationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", userEmail));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", applicationId));

        // Vérifier que l'application est acceptée
        if (application.getStatut() != ApplicationStatus.ACCEPTEE) {
            throw new RuntimeException("Cette conversation n'est pas disponible");
        }

        // Vérifier que l'utilisateur fait partie de la conversation
        Long clientId = application.getServiceRequest().getClient().getId();
        Long providerId = application.getProvider().getId();

        if (!user.getId().equals(clientId) && !user.getId().equals(providerId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à voir cette conversation");
        }

        // Marquer les messages comme lus
        messageRepository.markAsRead(applicationId, user.getId());

        return messageRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId)
                .stream()
                .map(m -> toMessageResponse(m, user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Récupérer la liste des conversations
     */
    public List<ConversationResponse> getConversations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", userEmail));

        // Récupérer toutes les applications acceptées de l'utilisateur
        List<Application> applications = new ArrayList<>();

        // Si client, récupérer les applications de ses annonces
        applications.addAll(applicationRepository.findPendingForClient(user.getId()));

        // Récupérer aussi celles où il est prestataire accepté
        applicationRepository.findByProviderIdAndStatut(user.getId(), ApplicationStatus.ACCEPTEE)
                .forEach(app -> {
                    if (!applications.contains(app)) {
                        applications.add(app);
                    }
                });

        // Récupérer aussi pour les clients (applications acceptées pour leurs annonces)
        // On va chercher toutes les applications acceptées liées à l'utilisateur
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Application app : getAcceptedApplicationsForUser(user)) {
            User otherUser;
            String otherRole;

            if (user.getId().equals(app.getServiceRequest().getClient().getId())) {
                // L'utilisateur est le client, l'autre est le prestataire
                otherUser = app.getProvider();
                otherRole = "Prestataire";
            } else {
                // L'utilisateur est le prestataire, l'autre est le client
                otherUser = app.getServiceRequest().getClient();
                otherRole = "Client";
            }

            // Récupérer le dernier message
            List<Message> messages = messageRepository.findByApplicationIdOrderByCreatedAtAsc(app.getId());
            Message lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

            boolean hasUnread = messages.stream()
                    .anyMatch(m -> !m.getIsRead() && !m.getSender().getId().equals(user.getId()));

            conversations.add(ConversationResponse.builder()
                    .applicationId(app.getId())
                    .requestTitre(app.getServiceRequest().getTitre())
                    .otherUserId(otherUser.getId())
                    .otherUserNom(otherUser.getNom())
                    .otherUserRole(otherRole)
                    .otherUserPhone(otherUser.getTelephone()) // Visible car match accepté
                    .lastMessage(lastMessage != null ? truncate(lastMessage.getContent(), 50) : null)
                    .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : app.getRespondedAt())
                    .hasUnread(hasUnread)
                    .build());
        }

        // Trier par dernier message
        conversations.sort((a, b) -> {
            if (a.getLastMessageAt() == null)
                return 1;
            if (b.getLastMessageAt() == null)
                return -1;
            return b.getLastMessageAt().compareTo(a.getLastMessageAt());
        });

        return conversations;
    }

    /**
     * Récupérer les applications acceptées pour un utilisateur
     */
    private List<Application> getAcceptedApplicationsForUser(User user) {
        List<Application> result = new ArrayList<>();

        // En tant que prestataire
        result.addAll(applicationRepository.findByProviderIdAndStatut(user.getId(), ApplicationStatus.ACCEPTEE));

        // En tant que client (via les annonces)
        // On utilise une requête personnalisée pour trouver les applications acceptées
        // des annonces du client
        // Pour simplifier, on va chercher toutes les applications et filtrer
        List<Application> allAccepted = applicationRepository.findAll().stream()
                .filter(a -> a.getStatut() == ApplicationStatus.ACCEPTEE)
                .filter(a -> a.getServiceRequest().getClient().getId().equals(user.getId()))
                .collect(Collectors.toList());

        for (Application app : allAccepted) {
            if (!result.contains(app)) {
                result.add(app);
            }
        }

        return result;
    }

    /**
     * Compter les messages non lus
     */
    public long countUnread(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", userEmail));

        return messageRepository.countUnreadForUser(user.getId());
    }

    private MessageResponse toMessageResponse(Message message, Long currentUserId) {
        return MessageResponse.builder()
                .id(message.getId())
                .applicationId(message.getApplication().getId())
                .senderId(message.getSender().getId())
                .senderNom(message.getSender().getNom())
                .isOwnMessage(message.getSender().getId().equals(currentUserId))
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
