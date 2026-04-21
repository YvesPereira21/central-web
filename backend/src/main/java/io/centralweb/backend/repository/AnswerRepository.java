package io.centralweb.backend.repository;

import io.centralweb.backend.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    @Query("SELECT a FROM Answer a " +
            "JOIN Question q ON a.question = q.questionId " +
            "WHERE q.published = true AND q.questionId = :questionId")
    List<Answer> findAllByQuestionIdAndQuestionPublished(
            @Param("questionId") UUID questionId
    );
}