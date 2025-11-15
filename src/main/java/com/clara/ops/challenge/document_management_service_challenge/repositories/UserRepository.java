package com.clara.ops.challenge.document_management_service_challenge.repositories;

import com.clara.ops.challenge.document_management_service_challenge.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository interface for managing User entities. */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by their unique username.
   *
   * @param username the username to search for
   * @return optional containing the user if found
   */
  Optional<User> findByUsername(String username);
}
