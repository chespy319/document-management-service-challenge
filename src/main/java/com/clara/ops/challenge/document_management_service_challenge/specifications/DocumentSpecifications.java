package com.clara.ops.challenge.document_management_service_challenge.specifications;

import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Provides reusable JPA specifications for filtering Document entities by user, name, and tags. */
public class DocumentSpecifications {

  /**
   * Creates a specification to filter documents by the associated user's username.
   *
   * @param username username to filter by
   * @return specification for filtering documents
   */
  public static Specification<Document> hasUserName(String username) {
    return (root, query, cb) ->
        (username == null || username.isBlank())
            ? cb.conjunction()
            : cb.equal(root.get("user").get("username"), username);
  }

  /**
   * Creates a specification to filter documents by their name (case-insensitive).
   *
   * @param name document name to filter by
   * @return specification for filtering documents
   */
  public static Specification<Document> hasDocumentName(String name) {
    return (root, query, cb) ->
        (name == null || name.isBlank())
            ? cb.conjunction()
            : cb.like(cb.lower(root.get("documentName")), "%" + name.toLowerCase() + "%");
  }

  /**
   * Creates a specification to filter documents by associated tags.
   *
   * @param tags list of tag names to filter by
   * @return specification for filtering documents
   */
  public static Specification<Document> hasTags(List<String> tags) {
    return (root, query, cb) -> {
      if (tags == null || tags.isEmpty()) {
        return cb.conjunction();
      }
      var tagsJoin = root.join("tags", JoinType.INNER);
      var lowerTags =
          tags.stream().filter(t -> t != null && !t.isBlank()).map(String::toLowerCase).toList();
      if (lowerTags.isEmpty()) {
        return cb.conjunction();
      }
      return tagsJoin.get("name").in(lowerTags);
    };
  }
}
