package com.home.services.controller;

import com.home.services.dto.request.ApplicationDto;
import com.home.services.dto.response.ApiResponse;
import com.home.services.dto.response.ApplicationResponse;
import com.home.services.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des candidatures
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Postuler à une annonce
     * POST /api/applications
     */
    @PostMapping
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @Valid @RequestBody ApplicationDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        ApplicationResponse response = applicationService.apply(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Candidature envoyée avec succès", response));
    }

    /**
     * Retirer une candidature
     * DELETE /api/applications/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        applicationService.withdraw(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Candidature retirée", null));
    }

    /**
     * Accepter une candidature
     * POST /api/applications/{id}/accept
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ApplicationResponse response = applicationService.accept(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse
                .success("Candidature acceptée ! Vous pouvez maintenant contacter le prestataire.", response));
    }

    /**
     * Refuser une candidature
     * POST /api/applications/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String reason = body != null ? body.get("reason") : null;
        ApplicationResponse response = applicationService.reject(id, userDetails.getUsername(), reason);
        return ResponseEntity.ok(ApiResponse.success("Candidature refusée", response));
    }

    /**
     * Lister mes candidatures (Prestataire)
     * GET /api/applications/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ApplicationResponse> responses = applicationService.getByProvider(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lister les candidatures pour une annonce (Client)
     * GET /api/applications/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ApplicationResponse> responses = applicationService.getByServiceRequest(requestId,
                userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
