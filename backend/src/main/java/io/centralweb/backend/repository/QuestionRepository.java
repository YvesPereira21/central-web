package io.centralweb.backend.repository;

import io.centralweb.backend.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    Page<Question> findAllByPublishedIsTrue(Pageable pageable);
    Page<Question> findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(String title, Pageable pageable);
    Page<Question> findAllByTags_TechnologyNameAndPublishedIsTrue(String tagsTechnologyName, Pageable pageable);
    @Query("SELECT q FROM Question q JOIN q.answers a " +
            "WHERE q.published = true AND a.accepted = true")
    Page<Question> findPublishedQuestionsWithAcceptedAnswers(Pageable pageable);
}
