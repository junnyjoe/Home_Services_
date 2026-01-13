package com.home.services.controller;

import com.home.services.dto.request.ReviewDto;
import com.home.services.dto.response.ApiResponse;
import com.home.services.dto.response.ReviewResponse;
import com.home.services.service.ReviewService;
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
 * Contrôleur pour les avis
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Laisser un avis
     * POST /api/reviews
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReviewResponse response = reviewService.create(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Merci pour votre avis !", response));
    }

    /**
     * Récupérer les avis d'un prestataire
     * GET /api/reviews/provider/{providerId}
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByProvider(@PathVariable Long providerId) {
        List<ReviewResponse> responses = reviewService.getByProvider(providerId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Récupérer la note moyenne d'un prestataire
     * GET /api/reviews/provider/{providerId}/rating
     */
    @GetMapping("/provider/{providerId}/rating")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRating(@PathVariable Long providerId) {
        Double avg = reviewService.getAverageRating(providerId);
        List<ReviewResponse> reviews = reviewService.getByProvider(providerId);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "average", avg != null ? avg : 0,
                "count", reviews.size())));
    }

    /**
     * Récupérer les avis récents (admin)
     * GET /api/reviews/recent
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getRecent() {
        List<ReviewResponse> responses = reviewService.getRecent();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
