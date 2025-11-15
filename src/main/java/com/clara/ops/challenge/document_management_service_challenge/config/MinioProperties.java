package com.clara.ops.challenge.document_management_service_challenge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Holds MinIO configuration properties such as endpoint, credentials, and bucket name. */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
  private String url;
  private String accessKey;
  private String secretKey;
  private String bucket;
}
