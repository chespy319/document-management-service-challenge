package com.clara.ops.challenge.document_management_service_challenge.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Response object containing search metadata and a list of matching documents. */
@Data
@Builder
@AllArgsConstructor
public class DownloadResponse {
  private String url;
}
