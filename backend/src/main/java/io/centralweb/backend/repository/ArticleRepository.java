package io.centralweb.backend.repository;

import io.centralweb.backend.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    List<Article> findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(
            String title
    );
    List<Article> findAllByProfile_ProfileIdAndPublishedIsTrue(
            UUID profileProfileId
    );
    List<Article> findAllByTags_TechnologyNameAndPublishedIsTrue(
            String tagsTechnologyName
    );

}

