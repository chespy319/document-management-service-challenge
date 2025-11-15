package com.clara.ops.challenge.document_management_service_challenge.repositories;

import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.entities.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Repository interface for managing Document entities with support for specifications and
 * pagination.
 */
public interface DocumentRepository
    extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

  /**
   * Finds a document by its name and associated user, including user and tag relationships.
   *
   * @param documentName name of the document
   * @param user associated user
   * @return optional containing the document if found
   */
  @EntityGraph(attributePaths = {"user", "tags"})
  Optional<Document> findByDocumentNameAndUser(String documentName, User user);

  /**
   * Retrieves all documents matching the given specification with pagination, including user and
   * tag relationships.
   *
   * @param spec specification for filtering documents
   * @param pageable pagination and sorting information
   * @return paginated list of documents
   */
  @NonNull @EntityGraph(attributePaths = {"user", "tags"})
  Page<Document> findAll(@Nullable Specification<Document> spec, @Nullable Pageable pageable);
}
