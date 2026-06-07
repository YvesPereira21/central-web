package io.centralweb.backend.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

public class GenericSearchSpecification {

    public static <T> Specification<T> searchByTitleOrContent(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            
            // Regra base obrigatória: O registro tem que estar publicado
            Predicate isPublishedPredicate = criteriaBuilder.isTrue(root.get("published"));

            // Se o termo de busca for nulo ou vazio, retorna apenas os itens que estão publicados
            if (searchTerm == null || searchTerm.isBlank()) {
                return isPublishedPredicate;
            }

            // Transforma o termo de busca para minúsculo e adiciona os curingas % do operador LIKE
            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            // Cria a regra de texto: title LIKE %termo% OR content LIKE %termo%
            Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), likePattern
            );
            
            Predicate contentPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")), likePattern
            );

            // Agrupa a condição do OR (Título ou Conteúdo)
            Predicate textSearchPredicate = criteriaBuilder.or(titlePredicate, contentPredicate);

            // Retorna a combinação final: published == true AND (title LIKE ... OR content LIKE ...)
            return criteriaBuilder.and(isPublishedPredicate, textSearchPredicate);
        };
    }
}
