package io.centralweb.backend.repository;

import io.centralweb.backend.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Page<Article> findAllByPublishedIsTrue(Pageable pageable);
    Page<Article> findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(String title, Pageable pageable);
    Page<Article> findAllByProfile_ProfileIdAndPublishedIsTrue(UUID profileProfileId, Pageable pageable);
    Page<Article> findAllByTags_TechnologyNameAndPublishedIsTrue(String tagsTechnologyName, Pageable pageable);
}

