package com.clara.ops.challenge.document_management_service_challenge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/** Entity representing a stored document with metadata, user association, and tags. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  @ToString.Include
  private Long id;

  @NotNull @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotBlank
  @ToString.Include
  @Column(name = "document_name", nullable = false)
  private String documentName;

  @NotBlank
  @ToString.Include
  @Column(name = "minio_path", nullable = false, unique = true)
  private String minioPath;

  @NotNull @PositiveOrZero
  @ToString.Include
  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @NotBlank
  @ToString.Include
  @Column(name = "file_type", nullable = false)
  private String fileType;

  @Builder.Default
  @NotNull @ToString.Include
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @Builder.Default
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "documents_tags",
      joinColumns = @JoinColumn(name = "document_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  /** Ensures the creation timestamp is set before persisting the entity. */
  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = OffsetDateTime.now();
    }
  }
}
