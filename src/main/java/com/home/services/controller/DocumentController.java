package com.home.services.controller;

import com.home.services.dto.response.ApiResponse;
import com.home.services.dto.response.DocumentResponse;
import com.home.services.model.enums.DocumentType;
import com.home.services.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour les documents
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload d'un document
     * POST /api/documents/upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<ApiResponse<DocumentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Le fichier est vide"));
        }

        // Vérifier le type de fichier
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Seuls les images et PDF sont acceptés"));
        }

        // Vérifier la taille (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Le fichier ne doit pas dépasser 5 Mo"));
        }

        DocumentResponse response = documentService.upload(file, type, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Document uploadé avec succès", response));
    }

    /**
     * Récupérer mes documents
     * GET /api/documents/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<DocumentResponse> responses = documentService.getByProvider(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Récupérer les documents en attente (admin)
     * GET /api/documents/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getPending() {
        List<DocumentResponse> responses = documentService.getPending();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Valider un document (admin)
     * POST /api/documents/{id}/validate
     */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponse>> validate(@PathVariable Long id) {
        DocumentResponse response = documentService.validate(id);
        return ResponseEntity.ok(ApiResponse.success("Document validé", response));
    }

    /**
     * Rejeter un document (admin)
     * POST /api/documents/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponse>> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String motif = body.get("motif");
        DocumentResponse response = documentService.reject(id, motif);
        return ResponseEntity.ok(ApiResponse.success("Document refusé", response));
    }
}
