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

    List<Document> findByUserId(Long userId);

    List<Document> findByStatut(DocumentStatus statut);

    Optional<Document> findByUserIdAndType(Long userId, DocumentType type);

    List<Document> findByUserIdAndStatut(Long userId, DocumentStatus statut);

    boolean existsByUserIdAndTypeAndStatut(Long userId, DocumentType type, DocumentStatus statut);
}
