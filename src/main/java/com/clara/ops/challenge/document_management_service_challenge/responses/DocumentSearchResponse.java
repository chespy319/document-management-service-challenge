package com.clara.ops.challenge.document_management_service_challenge.responses;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Response object containing search metadata and a list of matching documents. */
@Data
@Builder
@AllArgsConstructor
public class DocumentSearchResponse {
  private MetadataResponse metadata;
  private List<DocumentResponse> documents;
}
