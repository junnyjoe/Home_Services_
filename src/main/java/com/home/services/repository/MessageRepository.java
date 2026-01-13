package com.home.services.repository;

import com.home.services.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Messages d'une conversation (application)
    List<Message> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    // Derniers messages par application pour un utilisateur
    @Query("SELECT m FROM Message m WHERE m.application.id IN " +
            "(SELECT a.id FROM Application a WHERE " +
            "(a.serviceRequest.client.id = :userId OR a.provider.id = :userId) " +
            "AND a.statut = 'ACCEPTEE') " +
            "AND m.id IN (SELECT MAX(m2.id) FROM Message m2 GROUP BY m2.application.id) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findLastMessagesForUser(@Param("userId") Long userId);

    // Compter messages non lus pour un utilisateur
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.id != :userId " +
            "AND m.isRead = false " +
            "AND m.application.id IN (SELECT a.id FROM Application a WHERE " +
            "(a.serviceRequest.client.id = :userId OR a.provider.id = :userId) " +
            "AND a.statut = 'ACCEPTEE')")
    long countUnreadForUser(@Param("userId") Long userId);

    // Marquer comme lus
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
            "WHERE m.application.id = :applicationId AND m.sender.id != :userId AND m.isRead = false")
    int markAsRead(@Param("applicationId") Long applicationId, @Param("userId") Long userId);
}
