package com.home.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.services.dto.request.LoginRequest;
import com.home.services.dto.request.RegisterRequest;
import com.home.services.model.enums.Role;
import com.home.services.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Inscription client réussie")
    void register_ClientSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("client@test.com")
                .password("password123")
                .nom("Jean Dupont")
                .telephone("0707070707")
                .role(Role.CLIENT)
                .build();

        performRegister(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is("client@test.com")))
                .andExpect(jsonPath("$.data.user.role", is("CLIENT")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Inscription prestataire réussie")
    void register_ProviderSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("provider@test.com")
                .password("password123")
                .nom("Marie Martin")
                .telephone("0808080808")
                .role(Role.PRESTATAIRE)
                .build();

        performRegister(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.user.role", is("PRESTATAIRE")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Email déjà utilisé")
    void register_EmailAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@test.com")
                .password("password123")
                .nom("Test User")
                .telephone("0909090909")
                .role(Role.CLIENT)
                .build();

        // Premier enregistrement
        performRegister(request).andExpect(status().isOk());

        // Deuxième tentative avec le même email
        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("POST /api/auth/login - Connexion réussie")
    void login_Success() throws Exception {
        // D'abord créer un utilisateur
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("login@test.com")
                .password("password123")
                .nom("Login User")
                .telephone("0606060606")
                .role(Role.CLIENT)
                .build();
        performRegister(registerRequest).andExpect(status().isOk());

        // Puis se connecter
        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is("login@test.com")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Mot de passe incorrect")
    void login_WrongPassword() throws Exception {
        // D'abord créer un utilisateur
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("wrongpass@test.com")
                .password("password123")
                .nom("Wrong Pass User")
                .telephone("0505050505")
                .role(Role.CLIENT)
                .build();
        performRegister(registerRequest).andExpect(status().isOk());

        // Tenter de se connecter avec un mauvais mot de passe
        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrongpass@test.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Utilisateur inexistant")
    void login_UserNotFound() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions performRegister(RegisterRequest request) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
