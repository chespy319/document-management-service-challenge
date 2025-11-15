package com.clara.ops.challenge.document_management_service_challenge.services;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentUploadRequest;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for handling file operations with MinIO, including upload and presigned URL generation.
 */
@Service
@RequiredArgsConstructor
public class MinioService {

  private final MinioClient minioClient;
  private final MinioProperties properties;

  /**
   * Uploads a document to MinIO using provided metadata and file content.
   *
   * @param metadata document metadata containing user and name
   * @param document file to upload
   * @return object name stored in MinIO
   * @throws RuntimeException if upload fails
   */
  public String uploadFile(DocumentUploadRequest metadata, MultipartFile document) {
    var objectName = metadata.getUser() + "/" + metadata.getName() + ".pdf";
    try (var streamDocument = document.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(properties.getBucket()).object(objectName).stream(
                  streamDocument, document.getSize(), -1)
              .contentType(document.getContentType())
              .build());
      return objectName;
    } catch (Exception e) {
      throw new RuntimeException("Error uploading document to MinIO", e);
    }
  }

  /**
   * Generates a presigned URL for accessing a MinIO object.
   *
   * @param objectName name of the object in MinIO
   * @param expirySeconds expiration time in seconds
   * @return presigned URL for the object
   * @throws RuntimeException if URL generation fails
   */
  public String getPresignedUrl(String objectName, int expirySeconds) {
    try {
      return minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(properties.getBucket())
              .object(objectName)
              .expiry(expirySeconds)
              .build());
    } catch (Exception e) {
      throw new RuntimeException("Error generating presigned URL for MinIO object", e);
    }
  }
}
