package com.clara.ops.challenge.document_management_service_challenge.services;

import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.entities.Tag;
import com.clara.ops.challenge.document_management_service_challenge.entities.User;
import com.clara.ops.challenge.document_management_service_challenge.repositories.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repositories.TagRepository;
import com.clara.ops.challenge.document_management_service_challenge.repositories.UserRepository;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentSearchRequest;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.responses.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.responses.DocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.responses.DownloadResponse;
import com.clara.ops.challenge.document_management_service_challenge.responses.MetadataResponse;
import com.clara.ops.challenge.document_management_service_challenge.specifications.DocumentSpecifications;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Service for managing documents, including upload, search, and download operations. */
@Service
@RequiredArgsConstructor
public class DocumentManagementService {

  private final DocumentRepository documentRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final MinioService minioService;

  // limit uploads
  private final Semaphore uploadSemaphore = new Semaphore(10);

  /**
   * Saves a document by uploading it to MinIO, associating it with a user and tags, and persisting
   * metadata in the database.
   *
   * @param metadata document metadata including user, name, and tags
   * @param document file to upload
   * @return response containing stored document details
   * @throws IllegalStateException if too many concurrent uploads are in progress
   */
  @Transactional
  public DocumentResponse save(DocumentUploadRequest metadata, MultipartFile document) {
    try {
      if (!uploadSemaphore.tryAcquire()) {
        throw new IllegalStateException("Too many concurrent uploads. Please try again later.");
      }
      // search or create user
      var user =
          userRepository
              .findByUsername(metadata.getUser())
              .orElseGet(
                  () -> userRepository.save(User.builder().username(metadata.getUser()).build()));
      // search or create tags
      var newTags =
          metadata.getTags().stream()
              .map(
                  tagName ->
                      tagRepository
                          .findByName(tagName)
                          .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
              .collect(Collectors.toSet());
      // search or create document
      var uploadDocument =
          documentRepository
              .findByDocumentNameAndUser(metadata.getName(), user)
              .orElseGet(Document::new);
      // remove orphaned tags
      var orphanedTags =
          uploadDocument.getTags().stream()
              .filter(Predicate.not(newTags::contains))
              .filter(
                  oldTag -> oldTag.getDocuments().isEmpty() || oldTag.getDocuments().size() == 1)
              .toList();
      tagRepository.deleteAll(orphanedTags);
      // upload to minio
      var minioPath = minioService.uploadFile(metadata, document);
      // update document
      uploadDocument.setUser(user);
      uploadDocument.setDocumentName(metadata.getName());
      uploadDocument.setFileSize(document.getSize());
      uploadDocument.setFileType(document.getContentType());
      uploadDocument.setMinioPath(minioPath);
      uploadDocument.setTags(newTags);
      var dbDocument = documentRepository.save(uploadDocument);
      return mapDocumentResponse(dbDocument);
    } finally {
      uploadSemaphore.release();
    }
  }

  /**
   * Searches documents based on provided criteria and pagination settings.
   *
   * @param pageable pagination and sorting information
   * @param search search request containing filters
   * @return response containing search results and metadata
   */
  @Transactional(readOnly = true)
  public DocumentSearchResponse search(Pageable pageable, DocumentSearchRequest search) {
    var documentSpecification =
        DocumentSpecifications.hasUserName(search.getUser())
            .and(DocumentSpecifications.hasDocumentName(search.getName()))
            .and(DocumentSpecifications.hasTags(search.getTags()));
    var documentsPage = documentRepository.findAll(documentSpecification, pageable);
    var metadata =
        MetadataResponse.builder()
            .currentItems(documentsPage.getNumberOfElements())
            .currentPage(documentsPage.getNumber())
            .itemsPerPage(documentsPage.getSize())
            .totalItems(documentsPage.getTotalElements())
            .totalPages(documentsPage.getTotalPages())
            .build();
    var documents = documentsPage.getContent().stream().map(this::mapDocumentResponse).toList();
    return DocumentSearchResponse.builder().documents(documents).metadata(metadata).build();
  }

  /**
   * Retrieves a document by its identifier and generates a presigned URL for download.
   *
   * @param id unique document identifier
   * @return response containing the presigned download URL
   */
  @Transactional(readOnly = true)
  public DownloadResponse download(Long id) {
    var document = documentRepository.findById(id).orElseThrow();
    var presignedUrl = minioService.getPresignedUrl(document.getMinioPath(), 3600);
    return DownloadResponse.builder().url(presignedUrl).build();
  }

  /**
   * Maps a Document entity to its corresponding response object.
   *
   * @param document document entity
   * @return response representation of the document
   */
  private DocumentResponse mapDocumentResponse(Document document) {
    return DocumentResponse.builder()
        .id(document.getId().toString())
        .user(document.getUser().getUsername())
        .name(document.getDocumentName())
        .tags(document.getTags().stream().map(Tag::getName).toList())
        .size(document.getFileSize())
        .type(document.getFileType())
        .createdAt(document.getCreatedAt())
        .build();
  }
}
