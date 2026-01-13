package com.home.services.service;

import com.home.services.dto.response.DocumentResponse;
import com.home.services.exception.ResourceNotFoundException;
import com.home.services.model.Document;
import com.home.services.model.ProviderProfile;
import com.home.services.model.User;
import com.home.services.model.enums.DocumentStatus;
import com.home.services.model.enums.DocumentType;
import com.home.services.model.enums.ProfileStatus;
import com.home.services.repository.DocumentRepository;
import com.home.services.repository.ProviderProfileRepository;
import com.home.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des documents
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Upload d'un document
     */
    @Transactional
    public DocumentResponse upload(MultipartFile file, DocumentType type, String providerEmail) throws IOException {
        User provider = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", providerEmail));

        // Créer le dossier d'upload s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir, "documents", provider.getId().toString());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = type.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Supprimer l'ancien document du même type s'il existe
        documentRepository.findByProviderIdAndType(provider.getId(), type)
                .ifPresent(doc -> {
                    try {
                        Files.deleteIfExists(Paths.get(doc.getUrl()));
                    } catch (IOException e) {
                        // Log error but continue
                    }
                    documentRepository.delete(doc);
                });

        // Créer l'enregistrement
        Document document = Document.builder()
                .provider(provider)
                .type(type)
                .filename(originalFilename)
                .url(filePath.toString())
                .statut(DocumentStatus.EN_ATTENTE)
                .build();

        document = documentRepository.save(document);

        // Mettre à jour le statut du profil
        updateProfileStatus(provider.getId());

        return toResponse(document);
    }

    /**
     * Récupérer les documents d'un prestataire
     */
    public List<DocumentResponse> getByProvider(String providerEmail) {
        User provider = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", providerEmail));

        return documentRepository.findByProviderId(provider.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les documents en attente (admin)
     */
    public List<DocumentResponse> getPending() {
        return documentRepository.findByStatut(DocumentStatus.EN_ATTENTE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Valider un document (admin)
     */
    @Transactional
    public DocumentResponse validate(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        document.setStatut(DocumentStatus.VALIDE);
        document.setValidatedAt(LocalDateTime.now());
        documentRepository.save(document);

        // Mettre à jour le statut du profil
        updateProfileStatus(document.getProvider().getId());

        return toResponse(document);
    }

    /**
     * Rejeter un document (admin)
     */
    @Transactional
    public DocumentResponse reject(Long documentId, String motif) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        document.setStatut(DocumentStatus.REFUSE);
        document.setMotifRefus(motif);
        document.setValidatedAt(LocalDateTime.now());
        documentRepository.save(document);

        // Mettre à jour le statut du profil
        updateProfileStatus(document.getProvider().getId());

        return toResponse(document);
    }

    /**
     * Mettre à jour le statut du profil en fonction des documents
     */
    private void updateProfileStatus(Long providerId) {
        ProviderProfile profile = providerProfileRepository.findByUserId(providerId).orElse(null);
        if (profile == null)
            return;

        List<Document> documents = documentRepository.findByProviderId(providerId);

        // Compter les documents validés
        long validCount = documents.stream()
                .filter(d -> d.getStatut() == DocumentStatus.VALIDE)
                .count();

        long pendingCount = documents.stream()
                .filter(d -> d.getStatut() == DocumentStatus.EN_ATTENTE)
                .count();

        // Si au moins 2 documents sont validés (CNI + attestation), le profil est
        // vérifié
        if (validCount >= 2) {
            profile.setStatut(ProfileStatus.VERIFIE);
        } else if (pendingCount > 0) {
            profile.setStatut(ProfileStatus.EN_ATTENTE);
        } else {
            profile.setStatut(ProfileStatus.NON_VERIFIE);
        }

        providerProfileRepository.save(profile);
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .providerId(document.getProvider().getId())
                .providerNom(document.getProvider().getNom())
                .type(document.getType())
                .typeLabel(getTypeLabel(document.getType()))
                .filename(document.getFilename())
                .url("/api/documents/" + document.getId() + "/download")
                .statut(document.getStatut())
                .statutLabel(getStatutLabel(document.getStatut()))
                .motifRefus(document.getMotifRefus())
                .createdAt(document.getCreatedAt())
                .validatedAt(document.getValidatedAt())
                .build();
    }

    private String getTypeLabel(DocumentType type) {
        switch (type) {
            case CNI:
                return "Carte d'identité";
            case ATTESTATION:
                return "Attestation de compétence";
            case DIPLOME:
                return "Diplôme";
            case AUTRE:
                return "Autre document";
            default:
                return type.name();
        }
    }

    private String getStatutLabel(DocumentStatus statut) {
        switch (statut) {
            case EN_ATTENTE:
                return "En attente de validation";
            case VALIDE:
                return "Validé";
            case REFUSE:
                return "Refusé";
            default:
                return statut.name();
        }
    }
}
