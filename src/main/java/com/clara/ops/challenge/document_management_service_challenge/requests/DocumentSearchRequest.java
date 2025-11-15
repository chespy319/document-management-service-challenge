package com.clara.ops.challenge.document_management_service_challenge.requests;

import com.clara.ops.challenge.document_management_service_challenge.config.JacksonConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Request object for searching documents by user, name, and associated tags. */
@Data
@Builder
@AllArgsConstructor
public class DocumentSearchRequest {

  @JsonDeserialize(using = JacksonConfig.TrimmedString.class)
  private String user;

  @JsonDeserialize(using = JacksonConfig.TrimmedString.class)
  private String name;

  @Builder.Default
  @JsonDeserialize(using = JacksonConfig.TrimmedStringList.class)
  private List<@NotBlank String> tags = new ArrayList<>();
}
