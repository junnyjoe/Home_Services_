package com.home.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.services.dto.request.*;
import com.home.services.dto.response.*;
import com.home.services.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EndToEndVerificationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Test
    public void testFullBusinessFlow() {
        // 1. Register Client
        RegisterRequest clientRegister = RegisterRequest.builder()
                .email("client_e2e@test.com")
                .password("Password123")
                .nom("Client E2E")
                .role(Role.CLIENT)
                .build();

        ResponseEntity<ApiResponse> regClientResp = restTemplate.postForEntity(getBaseUrl() + "/auth/register",
                clientRegister, ApiResponse.class);
        assertThat(regClientResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Login Client
        LoginRequest clientLogin = LoginRequest.builder()
                .email("client_e2e@test.com")
                .password("Password123")
                .build();
        ResponseEntity<ApiResponse> loginClientResp = restTemplate.postForEntity(getBaseUrl() + "/auth/login",
                clientLogin, ApiResponse.class);
        String clientToken = (String) ((java.util.Map) loginClientResp.getBody().getData()).get("accessToken");

        // 3. Create Service Request
        ServiceRequestDto requestDto = ServiceRequestDto.builder()
                .categoryId(1L)
                .titre("Verification Test Request")
                .description("Detailed description for E2E verification test purpose.")
                .quartier("Plateau")
                .budgetMin(new BigDecimal("10000"))
                .budgetMax(new BigDecimal("20000"))
                .urgence(Urgency.NORMAL)
                .build();

        HttpHeaders clientHeaders = new HttpHeaders();
        clientHeaders.setBearerAuth(clientToken);
        HttpEntity<ServiceRequestDto> clientRequestEntity = new HttpEntity<>(requestDto, clientHeaders);

        ResponseEntity<ApiResponse> createRequestResp = restTemplate.exchange(getBaseUrl() + "/requests",
                HttpMethod.POST, clientRequestEntity, ApiResponse.class);
        assertThat(createRequestResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long requestId = Long.valueOf(((java.util.Map) createRequestResp.getBody().getData()).get("id").toString());

        // 4. Register Provider
        RegisterRequest providerRegister = RegisterRequest.builder()
                .email("provider_e2e@test.com")
                .password("Password123")
                .nom("Provider E2E")
                .role(Role.PRESTATAIRE)
                .build();
        restTemplate.postForEntity(getBaseUrl() + "/auth/register", providerRegister, ApiResponse.class);

        // 5. Login Provider
        LoginRequest providerLogin = LoginRequest.builder()
                .email("provider_e2e@test.com")
                .password("Password123")
                .build();
        ResponseEntity<ApiResponse> loginProvResp = restTemplate.postForEntity(getBaseUrl() + "/auth/login",
                providerLogin, ApiResponse.class);
        String provToken = (String) ((java.util.Map) loginProvResp.getBody().getData()).get("accessToken");

        // 6. Provider Apply
        ApplicationDto applyDto = ApplicationDto.builder()
                .serviceRequestId(requestId)
                .message("I can help with this")
                .proposedPrice(new BigDecimal("15000"))
                .proposedDays(2)
                .build();

        HttpHeaders provHeaders = new HttpHeaders();
        provHeaders.setBearerAuth(provToken);
        HttpEntity<ApplicationDto> provApplyEntity = new HttpEntity<>(applyDto, provHeaders);

        ResponseEntity<ApiResponse> applyResp = restTemplate.exchange(getBaseUrl() + "/applications", HttpMethod.POST,
                provApplyEntity, ApiResponse.class);
        assertThat(applyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long applicationId = Long.valueOf(((java.util.Map) applyResp.getBody().getData()).get("id").toString());

        // 7. Client Accept Bid
        ResponseEntity<ApiResponse> acceptResp = restTemplate.exchange(
                getBaseUrl() + "/applications/" + applicationId + "/accept", HttpMethod.POST, clientRequestEntity,
                ApiResponse.class);
        assertThat(acceptResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 8. Test Messaging
        MessageDto messageDto = MessageDto.builder()
                .applicationId(applicationId)
                .content("Contact me please")
                .build();
        HttpEntity<MessageDto> msgEntity = new HttpEntity<>(messageDto, provHeaders);
        ResponseEntity<ApiResponse> msgResp = restTemplate.exchange(getBaseUrl() + "/messages", HttpMethod.POST,
                msgEntity, ApiResponse.class);
        assertThat(msgResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
