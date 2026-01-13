package com.home.services.model;

import com.home.services.model.enums.DocumentStatus;
import com.home.services.model.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Document d'identité/vérification d'un prestataire
 */
@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false)
    private String filePath;

    @Column
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus statut = DocumentStatus.EN_ATTENTE;

    // Commentaire de l'admin en cas de rejet
    @Column(columnDefinition = "TEXT")
    private String commentaireAdmin;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime validatedAt;
}
