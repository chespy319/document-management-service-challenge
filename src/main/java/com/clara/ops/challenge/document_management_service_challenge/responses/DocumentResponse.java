package com.clara.ops.challenge.document_management_service_challenge.responses;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Response object representing a document with metadata, user, tags, and creation details. */
@Data
@Builder
@AllArgsConstructor
public class DocumentResponse {
  private String id;
  private String user;
  private String name;
  private List<String> tags;
  private Long size;
  private String type;
  private OffsetDateTime createdAt;
}
