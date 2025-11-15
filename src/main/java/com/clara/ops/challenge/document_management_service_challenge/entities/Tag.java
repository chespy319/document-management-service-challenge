package com.clara.ops.challenge.document_management_service_challenge.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/** Entity representing a tag that can be associated with multiple documents. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tags")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  @EqualsAndHashCode.Include
  private Long id;

  @NotBlank
  @ToString.Include
  @Column(nullable = false, unique = true)
  private String name;

  @Builder.Default
  @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
  private Set<Document> documents = new HashSet<>();
}
