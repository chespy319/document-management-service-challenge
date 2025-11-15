package com.clara.ops.challenge.document_management_service_challenge.repositories;

import com.clara.ops.challenge.document_management_service_challenge.entities.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository interface for managing Tag entities. */
public interface TagRepository extends JpaRepository<Tag, Long> {

  /**
   * Finds a tag by its unique name.
   *
   * @param name tag name
   * @return optional containing the tag if found
   */
  Optional<Tag> findByName(String name);
}
