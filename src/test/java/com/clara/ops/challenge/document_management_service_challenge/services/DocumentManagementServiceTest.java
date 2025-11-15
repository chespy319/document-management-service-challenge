package com.clara.ops.challenge.document_management_service_challenge.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.entities.Tag;
import com.clara.ops.challenge.document_management_service_challenge.entities.User;
import com.clara.ops.challenge.document_management_service_challenge.repositories.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repositories.TagRepository;
import com.clara.ops.challenge.document_management_service_challenge.repositories.UserRepository;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentSearchRequest;
import com.clara.ops.challenge.document_management_service_challenge.requests.DocumentUploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.responses.DocumentResponse;
import java.io.ByteArrayInputStream;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentManagementServiceTest {

  private final String pdfContentType = "application/pdf";

  @Mock private DocumentRepository documentRepository;
  @Mock private UserRepository userRepository;
  @Mock private TagRepository tagRepository;
  @Mock private MinioService minioService;
  @Mock private MultipartFile multipartFile;

  private DocumentManagementService documentManagementService;

  @BeforeEach
  void setUp() {
    documentManagementService =
        new DocumentManagementService(
            documentRepository, userRepository, tagRepository, minioService);
  }

  @Test
  void shouldPersistDocumentWithNewUserAndTags() throws Exception {
    var metadata = new DocumentUploadRequest("edgar", "doc1", List.of("tag1"));
    var inputStream = new ByteArrayInputStream("test content".getBytes());
    var user = User.builder().id(1L).username("edgar").build();
    var tag = Tag.builder().id(1L).name("tag1").build();
    var document =
        Document.builder()
            .id(100L)
            .user(user)
            .fileSize((long) inputStream.readAllBytes().length)
            .fileType(pdfContentType)
            .documentName("doc1")
            .minioPath("edgar/doc1.pdf")
            .tags(Set.of(tag))
            .build();

    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(tagRepository.findByName(tag.getName())).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenReturn(tag);
    when(documentRepository.findByDocumentNameAndUser(document.getDocumentName(), user))
        .thenReturn(Optional.empty());
    when(documentRepository.save(any(Document.class))).thenReturn(document);
    when(multipartFile.getSize()).thenReturn((long) inputStream.readAllBytes().length);
    when(multipartFile.getContentType()).thenReturn(pdfContentType);
    when(minioService.uploadFile(metadata, multipartFile)).thenReturn(document.getMinioPath());

    DocumentResponse response = documentManagementService.save(metadata, multipartFile);

    assertThat(response.getId()).isEqualTo(document.getId().toString());
    assertThat(response.getUser()).isEqualTo(user.getUsername());
    assertThat(response.getName()).isEqualTo(document.getDocumentName());
    assertThat(response.getTags()).containsOnly(tag.getName());
    assertThat(response.getType()).isEqualTo(pdfContentType);
    assertThat(response.getSize()).isEqualTo(document.getFileSize());
    assertThat(response.getCreatedAt()).isNotNull();

    verify(userRepository).save(any(User.class));
    verify(tagRepository).save(any(Tag.class));
    verify(minioService).uploadFile(metadata, multipartFile);
    verify(documentRepository).save(any(Document.class));
  }

  @Test
  void shouldReturnDocumentsWithMetadata() {
    var pageable = PageRequest.of(0, 10);
    var search = new DocumentSearchRequest("edgar", "doc1", List.of("tag1"));
    var user = User.builder().id(1L).username("edgar").build();
    var tag = Tag.builder().id(1L).name("tag1").build();
    var document =
        Document.builder()
            .id(100L)
            .user(user)
            .fileSize((long) "test content".getBytes().length)
            .fileType(pdfContentType)
            .documentName("doc1")
            .minioPath("edgar/doc1.pdf")
            .tags(Set.of(tag))
            .build();
    var page = new PageImpl<>(List.of(document), pageable, 1);

    when(documentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    var response = documentManagementService.search(pageable, search);

    assertThat(response.getDocuments()).hasSize(1);
    assertThat(response.getDocuments().get(0).getName()).isEqualTo(document.getDocumentName());
    assertThat(response.getDocuments().get(0).getId()).isEqualTo(document.getId().toString());
    assertThat(response.getDocuments().get(0).getUser()).isEqualTo(user.getUsername());
    assertThat(response.getDocuments().get(0).getName()).isEqualTo(document.getDocumentName());
    assertThat(response.getDocuments().get(0).getTags()).containsOnly(tag.getName());
    assertThat(response.getDocuments().get(0).getType()).isEqualTo(pdfContentType);
    assertThat(response.getDocuments().get(0).getSize()).isEqualTo(document.getFileSize());
    assertThat(response.getDocuments().get(0).getCreatedAt()).isNotNull();
    assertThat(response.getMetadata().getTotalItems()).isEqualTo(1);
  }

  @Test
  void shouldReturnPresignedUrl() {
    var expiration = 3600;
    var user = User.builder().id(1L).username("edgar").build();
    var document =
        Document.builder()
            .id(100L)
            .user(user)
            .fileType(pdfContentType)
            .documentName("doc1")
            .minioPath("edgar/doc1.pdf")
            .build();

    when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
    when(minioService.getPresignedUrl(document.getMinioPath(), expiration))
        .thenReturn("http://localhost:9000/test-bucket/edgar/doc1.pdf");

    var response = documentManagementService.download(document.getId());

    assertThat(response.getUrl()).contains(document.getMinioPath());
    verify(minioService).getPresignedUrl(document.getMinioPath(), expiration);
  }
}
