package io.centralweb.backend.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

public class GenericSearchSpecification {

    public static <T> Specification<T> searchByTitleOrContent(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            
            Predicate isPublishedPredicate = criteriaBuilder.isTrue(root.get("published"));

            if (searchTerm == null || searchTerm.isBlank()) {
                return isPublishedPredicate;
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), likePattern
            );
            
            Predicate contentPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")), likePattern
            );

            Predicate textSearchPredicate = criteriaBuilder.or(titlePredicate, contentPredicate);

            return criteriaBuilder.and(isPublishedPredicate, textSearchPredicate);
        };
    }
}
