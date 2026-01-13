package com.home.services.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant un avis/évaluation
 * Le client évalue le prestataire après la fin du service
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "application_id" }) // Un seul avis par prestation
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lié à une candidature acceptée et terminée
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // Le client qui donne l'avis
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // Le prestataire évalué
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    // Note de 1 à 5
    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer note;

    // Commentaire
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String commentaire;

    // Sous-notes (optionnelles)
    @Min(1)
    @Max(5)
    private Integer noteQualite; // Qualité du travail

    @Min(1)
    @Max(5)
    private Integer notePonctualite; // Respect des délais

    @Min(1)
    @Max(5)
    private Integer noteCommunication; // Communication

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
