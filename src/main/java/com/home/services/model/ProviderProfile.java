package com.home.services.model;

import com.home.services.model.enums.ProfileStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Profil professionnel d'un prestataire
 * Contient les informations spécifiques aux prestataires de services
 */
@Entity
@Table(name = "provider_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Compétences stockées en JSON (liste de strings)
    @Column(columnDefinition = "TEXT")
    private String competences;

    // Quartier d'intervention (Abidjan V1)
    @Column(length = 100)
    private String quartier;

    @DecimalMin(value = "0.0", message = "Le tarif doit être positif")
    @Column(precision = 10, scale = 2)
    private BigDecimal tarifHoraire;

    // Note moyenne (calculée à partir des évaluations)
    @Builder.Default
    private Double noteGlobale = 0.0;

    @Builder.Default
    private Integer nombreAvis = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProfileStatus statut = ProfileStatus.INCOMPLET;

    // Catégories de services proposés
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "provider_categories", joinColumns = @JoinColumn(name = "provider_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
