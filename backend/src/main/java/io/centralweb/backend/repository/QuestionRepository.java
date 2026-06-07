package io.centralweb.backend.repository;

import io.centralweb.backend.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID>, JpaSpecificationExecutor<Question> {
    Page<Question> findAllByPublishedIsTrue(Pageable pageable);
    Page<Question> findAllByTitleContainingIgnoreCaseAndPublishedIsTrue(String title, Pageable pageable);
    Page<Question> findAllByTags_TechnologyNameAndPublishedIsTrue(String tagsTechnologyName, Pageable pageable);
    Page<Question> findAllByProfile_ProfileIdAndPublishedIsTrue(UUID profileId, Pageable pageable);

    @Query("SELECT q FROM Question q JOIN q.answers a " +
            "WHERE q.published = true AND a.accepted = true")
    Page<Question> findPublishedQuestionsWithAcceptedAnswers(Pageable pageable);

    @Query("SELECT q FROM Question q JOIN q.tags t WHERE t.technologyName IN :tagNames AND q.published = true GROUP BY q.id HAVING COUNT(DISTINCT t.technologyName) = :tagCount")
    Page<Question> findAllByTagsStrict(@Param("tagNames") List<String> tagNames, @Param("tagCount") long tagCount, Pageable pageable);
}
