package com.home.services.service;

import com.home.services.dto.request.LoginRequest;
import com.home.services.dto.request.RegisterRequest;
import com.home.services.dto.response.AuthResponse;
import com.home.services.model.ProviderProfile;
import com.home.services.model.User;
import com.home.services.model.enums.ProfileStatus;
import com.home.services.model.enums.Role;
import com.home.services.repository.ProviderProfileRepository;
import com.home.services.repository.UserRepository;
import com.home.services.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service gérant l'authentification (inscription, connexion)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Définir le rôle par défaut si non spécifié
        Role role = request.getRole() != null ? request.getRole() : Role.CLIENT;

        // Empêcher l'inscription en tant qu'admin
        if (role == Role.ADMIN) {
            throw new RuntimeException("Inscription en tant qu'administrateur non autorisée");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telephone(request.getTelephone())
                .role(role)
                .verified(false)
                .active(true)
                .build();

        user = userRepository.save(user);

        // Si prestataire, créer un profil vide
        if (role == Role.PRESTATAIRE) {
            ProviderProfile profile = ProviderProfile.builder()
                    .user(user)
                    .statut(ProfileStatus.INCOMPLET)
                    .build();
            providerProfileRepository.save(profile);
        }

        // Générer le token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getNom(), user.getEmail(), user.getRole(),
                user.getVerified());
    }

    /**
     * Connexion d'un utilisateur existant
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getActive()) {
            throw new RuntimeException("Ce compte a été désactivé");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getNom(), user.getEmail(), user.getRole(),
                user.getVerified());
    }

    /**
     * Récupérer l'utilisateur connecté à partir du token
     */
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}
