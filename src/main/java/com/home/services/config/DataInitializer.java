package com.home.services.config;

import com.home.services.model.Category;
import com.home.services.model.User;
import com.home.services.model.enums.Role;
import com.home.services.repository.CategoryRepository;
import com.home.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration pour initialiser les données de base au démarrage
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("dev")
    public CommandLineRunner initDevData() {
        return args -> {
            log.info("Initialisation des données de développement...");

            // Créer les catégories si elles n'existent pas
            initCategories();

            // Créer un admin par défaut
            initAdminUser();

            log.info("Données de développement initialisées avec succès.");
        };
    }

    private void initCategories() {
        List<Category> categories = Arrays.asList(
                Category.builder().nom("Ménage").description("Services de nettoyage et entretien").icone("🧹")
                        .active(true).build(),
                Category.builder().nom("Plomberie").description("Réparation et installation plomberie").icone("🔧")
                        .active(true).build(),
                Category.builder().nom("Électricité").description("Travaux et dépannage électrique").icone("⚡")
                        .active(true).build(),
                Category.builder().nom("Climatisation").description("Installation et maintenance climatisation")
                        .icone("❄️").active(true).build(),
                Category.builder().nom("Jardinage").description("Entretien jardins et espaces verts").icone("🌿")
                        .active(true).build(),
                Category.builder().nom("Déménagement").description("Services de déménagement").icone("📦").active(true)
                        .build(),
                Category.builder().nom("Informatique").description("Réparation et assistance informatique").icone("💻")
                        .active(true).build(),
                Category.builder().nom("Coiffure").description("Coiffure à domicile").icone("💇").active(true).build(),
                Category.builder().nom("Couture").description("Couture et retouches").icone("🧵").active(true).build(),
                Category.builder().nom("Cours particuliers").description("Soutien scolaire et cours").icone("📚")
                        .active(true).build(),
                Category.builder().nom("Traiteur").description("Services traiteur et restauration").icone("🍽️")
                        .active(true).build(),
                Category.builder().nom("Photographie").description("Services photo et vidéo").icone("📷").active(true)
                        .build());

        for (Category category : categories) {
            if (!categoryRepository.existsByNom(category.getNom())) {
                categoryRepository.save(category);
                log.info("Catégorie créée: {}", category.getNom());
            }
        }
    }

    private void initAdminUser() {
        String adminEmail = "admin@homeservices.ci";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .nom("Administrateur")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .telephone("+225 0700000000")
                    .role(Role.ADMIN)
                    .verified(true)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Utilisateur admin créé: {}", adminEmail);
        }
    }
}
