package com.home.services.controller;

import com.home.services.dto.request.ServiceRequestDto;
import com.home.services.dto.response.ApiResponse;
import com.home.services.dto.response.ServiceRequestResponse;
import com.home.services.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des annonces de services
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    /**
     * Créer une nouvelle annonce (Client uniquement)
     * POST /api/requests
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> create(
            @Valid @RequestBody ServiceRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        ServiceRequestResponse response = serviceRequestService.create(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Annonce créée avec succès", response));
    }

    /**
     * Mettre à jour une annonce (Propriétaire uniquement)
     * PUT /api/requests/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        ServiceRequestResponse response = serviceRequestService.update(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Annonce mise à jour", response));
    }

    /**
     * Annuler une annonce (Propriétaire uniquement)
     * DELETE /api/requests/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        serviceRequestService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Annonce annulée", null));
    }

    /**
     * Obtenir une annonce par ID
     * GET /api/requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ServiceRequestResponse response = serviceRequestService.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lister mes annonces (Client)
     * GET /api/requests/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ServiceRequestResponse> responses = serviceRequestService.getByClient(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lister les annonces publiées avec filtres (Prestataires)
     * GET /api/requests?categoryId=X&quartier=Y
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getPublished(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String quartier) {

        List<ServiceRequestResponse> responses = serviceRequestService.getPublished(categoryId, quartier);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Rechercher des annonces par mot-clé
     * GET /api/requests/search?q=keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> search(
            @RequestParam String q) {

        List<ServiceRequestResponse> responses = serviceRequestService.search(q);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
