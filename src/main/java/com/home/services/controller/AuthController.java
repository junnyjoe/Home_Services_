package com.home.services.controller;

import com.home.services.dto.request.LoginRequest;
import com.home.services.dto.request.RegisterRequest;
import com.home.services.dto.response.ApiResponse;
import com.home.services.dto.response.AuthResponse;
import com.home.services.dto.response.UserResponse;
import com.home.services.model.User;
import com.home.services.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour l'authentification (inscription, connexion)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inscription d'un nouvel utilisateur
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Inscription réussie", response));
    }

    /**
     * Connexion d'un utilisateur
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }

    /**
     * Récupérer les informations de l'utilisateur connecté
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .nom(user.getNom())
                .email(user.getEmail())
                .telephone(user.getTelephone())
                .role(user.getRole())
                .verified(user.getVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
