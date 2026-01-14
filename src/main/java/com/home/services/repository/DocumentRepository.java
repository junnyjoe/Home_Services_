package com.home.services.repository;

import com.home.services.model.Document;
import com.home.services.model.enums.DocumentStatus;
import com.home.services.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByProviderId(Long providerId);

    List<Document> findByStatut(DocumentStatus statut);

    Optional<Document> findByProviderIdAndType(Long providerId, DocumentType type);

    List<Document> findByProviderIdAndStatut(Long providerId, DocumentStatus statut);

    boolean existsByProviderIdAndTypeAndStatut(Long providerId, DocumentType type, DocumentStatus statut);
}
