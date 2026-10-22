package io.centralweb.backend.repository;

import io.centralweb.backend.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID>, JpaSpecificationExecutor<Article> {
    Page<Article> findAllByPublishedIsTrue(Pageable pageable);
    Page<Article> findAllByProfile_ProfileIdAndPublishedIsTrue(UUID profileProfileId, Pageable pageable);
    Page<Article> findAllByTags_TechnologyNameAndPublishedIsTrue(String tagsTechnologyName, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.technologyName IN :tagNames AND a.published = true GROUP BY a.articleId HAVING COUNT(DISTINCT t.technologyName) = :tagCount")
    Page<Article> findAllByTagsStrict(@Param("tagNames") List<String> tagNames, @Param("tagCount") long tagCount, Pageable pageable);

    @Query(value = """
    SELECT a.*, (SELECT COUNT(*) FROM article_likes al WHERE al.article_id = a.article_id) AS articleTotalLikes
    FROM articles a
    WHERE a.published = true
      AND a.search_vector @@ websearch_to_tsquery('portuguese', :term)
    ORDER BY ts_rank(a.search_vector, websearch_to_tsquery('portuguese', :term)) DESC
    """,
            countQuery = """
    SELECT count(*) FROM articles a
    WHERE a.published = true
      AND a.search_vector @@ websearch_to_tsquery('portuguese', :term)
    """,
            nativeQuery = true)
    Page<Article> searchByFullText(@Param("term") String term, Pageable pageable);
}
