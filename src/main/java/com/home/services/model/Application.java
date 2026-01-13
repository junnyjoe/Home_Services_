package com.home.services.model;

import com.home.services.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant une candidature d'un prestataire à une annonce
 */
@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "service_request_id", "provider_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    // Message de candidature
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String message;

    // Proposition de tarif
    @Column(precision = 10, scale = 2)
    private BigDecimal proposedPrice;

    // Délai proposé (en jours)
    private Integer proposedDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus statut = ApplicationStatus.EN_ATTENTE;

    // Réponse du client (si refusé)
    @Size(max = 500)
    private String clientResponse;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Date d'acceptation/refus
    private LocalDateTime respondedAt;
}
