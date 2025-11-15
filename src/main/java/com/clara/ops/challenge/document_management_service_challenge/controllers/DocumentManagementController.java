package com.clara.ops.challenge.document_management_service_challenge.controllers;

import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentSearchRequest;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.responses.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.responses.DocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.responses.DownloadResponse;
import com.clara.ops.challenge.document_management_service_challenge.services.DocumentManagementService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for managing document operations such as upload, search, and download. */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("document-management")
public class DocumentManagementController {

  private final DocumentManagementService documentManagementService;

  /**
   * Handles document upload with associated metadata.
   *
   * @param metadata document metadata
   * @param document file to upload
   * @return response containing document details
   */
  @PostMapping("/upload")
  @ResponseStatus(HttpStatus.CREATED)
  public DocumentResponse upload(
      @Validated @RequestPart("metadata") DocumentUploadRequest metadata,
      @RequestPart("document") MultipartFile document) {
    return documentManagementService.save(metadata, document);
  }

  /**
   * Searches documents based on criteria and pagination settings.
   *
   * @param search search request with filters
   * @param pageable pagination and sorting information
   * @return response containing search results
   */
  @PostMapping("/search")
  public DocumentSearchResponse search(
      @Validated @RequestBody DocumentSearchRequest search,
      @PageableDefault(size = 20) @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return documentManagementService.search(pageable, search);
  }

  /**
   * Downloads a document by its identifier.
   *
   * @param id unique document identifier
   * @return response containing document content
   */
  @GetMapping("/download/{id}")
  public DownloadResponse download(@Validated @PathVariable @NotNull @Positive Long id) {
    return documentManagementService.download(id);
  }
}
