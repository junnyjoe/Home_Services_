package com.home.services.model;

import com.home.services.model.enums.RequestStatus;
import com.home.services.model.enums.Urgency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant une annonce de demande de service
 * Publiée par un client pour trouver un prestataire
 */
@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200)
    @Column(nullable = false)
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 20, max = 2000)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Quartier d'Abidjan
    @NotBlank(message = "Le quartier est obligatoire")
    @Column(length = 100, nullable = false)
    private String quartier;

    // Adresse précise (optionnel, visible après match)
    @Column(length = 255)
    private String adresse;

    // Budget indicatif
    @Column(precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal budgetMax;

    // Date souhaitée pour la prestation
    private LocalDate datePrestation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Urgency urgence = Urgency.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus statut = RequestStatus.BROUILLON;

    // Nombre de candidatures reçues
    @Builder.Default
    private Integer nombreCandidatures = 0;

    // Prestataire sélectionné (après acceptation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_provider_id")
    private User selectedProvider;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Date d'expiration de l'annonce (optionnel)
    private LocalDateTime expiresAt;
}
