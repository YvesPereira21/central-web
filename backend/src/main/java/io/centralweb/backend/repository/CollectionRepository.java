package io.centralweb.backend.repository;

import io.centralweb.backend.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    Page<Collection> findAllByProfile_ProfileId(UUID profileId, Pageable pageable);
    List<Collection> findAllByProfile_User_UserIdAndArticles_ArticleId(UUID userId, UUID articleId);
    List<Collection> findAllByProfile_User_UserIdAndQuestions_QuestionId(UUID userId, UUID questionId);
}
