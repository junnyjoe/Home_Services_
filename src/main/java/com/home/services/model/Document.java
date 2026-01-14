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
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    // Nom du fichier original
    private String filename;

    // Chemin ou URL du fichier
    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus statut = DocumentStatus.EN_ATTENTE;

    // Motif de refus si refusé
    @Column(columnDefinition = "TEXT")
    private String motifRefus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime validatedAt;
}
