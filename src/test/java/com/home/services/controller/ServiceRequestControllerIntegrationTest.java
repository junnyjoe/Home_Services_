package com.home.services.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.services.dto.request.RegisterRequest;
import com.home.services.dto.request.ServiceRequestDto;
import com.home.services.model.Category;
import com.home.services.model.enums.Role;
import com.home.services.model.enums.Urgency;
import com.home.services.repository.CategoryRepository;
import com.home.services.repository.ServiceRequestRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ServiceRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    private String clientToken;
    private String providerToken;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        serviceRequestRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Créer une catégorie
        Category category = categoryRepository.save(Category.builder()
                .nom("Plomberie")
                .description("Services de plomberie")
                .icone("🔧")
                .build());
        categoryId = category.getId();

        // Créer un client et récupérer le token
        clientToken = registerAndGetToken("client@test.com", "password123", "Client Test", Role.CLIENT);

        // Créer un prestataire et récupérer le token
        providerToken = registerAndGetToken("provider@test.com", "password123", "Provider Test", Role.PRESTATAIRE);
    }

    @Test
    @DisplayName("POST /api/requests - Création d'annonce réussie")
    void createRequest_Success() throws Exception {
        ServiceRequestDto dto = ServiceRequestDto.builder()
                .categoryId(categoryId)
                .titre("Réparation fuite d'eau")
                .description("Fuite sous l'évier de la cuisine")
                .quartier("Cocody - Riviera")
                .adresse("Rue des Jardins")
                .budgetMin(new BigDecimal("10000"))
                .budgetMax(new BigDecimal("25000"))
                .urgence(Urgency.URGENT)
                .build();

        mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.titre", is("Réparation fuite d'eau")))
                .andExpect(jsonPath("$.data.statut", is("BROUILLON")));
    }

    @Test
    @DisplayName("POST /api/requests - Prestataire ne peut pas créer d'annonce")
    void createRequest_ProviderForbidden() throws Exception {
        ServiceRequestDto dto = ServiceRequestDto.builder()
                .categoryId(categoryId)
                .titre("Test")
                .description("Test description")
                .quartier("Plateau")
                .build();

        mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + providerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/requests - Liste des annonces publiées")
    void getPublishedRequests_Success() throws Exception {
        // Créer et publier une annonce
        ServiceRequestDto dto = ServiceRequestDto.builder()
                .categoryId(categoryId)
                .titre("Annonce publique")
                .description("Description de l'annonce")
                .quartier("Marcory")
                .build();

        // Créer l'annonce
        MvcResult createResult = mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        // Extraire l'ID
        String response = createResult.getResponse().getContentAsString();
        Long requestId = objectMapper.readTree(response).path("data").path("id").asLong();

        // Publier l'annonce
        mockMvc.perform(post("/api/requests/" + requestId + "/publish")
                .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());

        // Vérifier que l'annonce est visible
        mockMvc.perform(get("/api/requests")
                .header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/requests/my - Mes annonces")
    void getMyRequests_Success() throws Exception {
        // Créer une annonce
        ServiceRequestDto dto = ServiceRequestDto.builder()
                .categoryId(categoryId)
                .titre("Mon annonce")
                .description("Ma description")
                .quartier("Yopougon")
                .build();

        mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // Récupérer mes annonces
        mockMvc.perform(get("/api/requests/my")
                .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].titre", is("Mon annonce")));
    }

    private String registerAndGetToken(String email, String password, String nom, Role role) throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .password(password)
                .nom(nom)
                .telephone("0707070707")
                .role(role)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }
}
