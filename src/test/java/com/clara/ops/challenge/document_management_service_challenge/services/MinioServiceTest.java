package com.clara.ops.challenge.document_management_service_challenge.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentUploadRequest;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

  private final String pdfContentType = "application/pdf";
  private final String bucket = "test-bucket";

  @Mock private MinioClient minioClient;

  @Mock private MultipartFile multipartFile;

  @InjectMocks private MinioService minioService;

  @BeforeEach
  void setUp() {
    var properties = new MinioProperties();
    properties.setBucket(bucket);
    minioService = new MinioService(minioClient, properties);
  }

  @Test
  void shouldUploadFileSuccessfully() throws Exception {
    var metadata = new DocumentUploadRequest("edgar", "document1", List.of());
    var inputStream = new ByteArrayInputStream("test content".getBytes());

    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(multipartFile.getSize()).thenReturn((long) inputStream.readAllBytes().length);
    when(multipartFile.getContentType()).thenReturn(pdfContentType);

    var result = minioService.uploadFile(metadata, multipartFile);
    assertThat(result).isEqualTo("edgar/document1.pdf");

    var captor = ArgumentCaptor.forClass(PutObjectArgs.class);
    verify(minioClient).putObject(captor.capture());
    var args = captor.getValue();
    assertThat(args.bucket()).isEqualTo(bucket);
    assertThat(args.object()).isEqualTo("edgar/document1.pdf");
    assertThat(args.contentType()).isEqualTo(pdfContentType);
  }

  @Test
  void shouldThrowExceptionWhenUploadFails() throws Exception {
    var metadata = new DocumentUploadRequest("edgar", "document2", List.of());
    var inputStream = new ByteArrayInputStream("dummy content".getBytes());

    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(multipartFile.getSize()).thenReturn((long) inputStream.readAllBytes().length);
    when(multipartFile.getContentType()).thenReturn(pdfContentType);
    doThrow(new RuntimeException("MinIO error"))
        .when(minioClient)
        .putObject(any(PutObjectArgs.class));

    assertThatThrownBy(() -> minioService.uploadFile(metadata, multipartFile))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error uploading document to MinIO");
  }

  @Test
  void shouldGeneratePresignedUrlSuccessfully() throws Exception {
    var objectName = "edgar/resume.pdf";
    var expiration = 3600;

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenReturn("http://localhost:9000/test-bucket/edgar/resume.pdf");

    var url = minioService.getPresignedUrl(objectName, expiration);
    assertThat(url).contains("resume.pdf");

    var captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
    verify(minioClient).getPresignedObjectUrl(captor.capture());
    var args = captor.getValue();
    assertThat(args.bucket()).isEqualTo(bucket);
    assertThat(args.object()).isEqualTo(objectName);
    assertThat(args.method()).isEqualTo(Method.GET);
    assertThat(args.expiry()).isEqualTo(expiration);
  }

  @Test
  void shouldThrowExceptionWhenPresignedUrlFails() throws Exception {
    var objectName = "edgar/resume.pdf";
    var expiration = 3600;

    when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenThrow(new RuntimeException("MinIO error"));

    assertThatThrownBy(() -> minioService.getPresignedUrl(objectName, expiration))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error generating presigned URL for MinIO object");
  }
}
