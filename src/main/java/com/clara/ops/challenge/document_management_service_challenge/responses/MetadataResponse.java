package com.clara.ops.challenge.document_management_service_challenge.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Response object containing pagination metadata for document search results. */
@Data
@Builder
@AllArgsConstructor
public class MetadataResponse {
  private int currentPage;
  private int itemsPerPage;
  private int currentItems;
  private int totalPages;
  private long totalItems;
}
