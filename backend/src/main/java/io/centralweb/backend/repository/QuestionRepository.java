package io.centralweb.backend.repository;

import io.centralweb.backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(
            String title
    );
    List<Question> findAllByTags_TechnologyNameAndPublishedIsTrue(
            String tagsTechnologyName
    );
    @Query(
            "SELECT q FROM Question q " +
                    "JOIN Answer a ON a.question = q.questionId " +
                    "WHERE q.published = true AND a.accepted = true"
    )
    List<Question> findPublishedQuestionsWithAcceptedAnswers();
}
