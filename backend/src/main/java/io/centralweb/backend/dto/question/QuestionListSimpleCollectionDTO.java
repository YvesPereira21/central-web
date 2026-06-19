package io.centralweb.backend.dto.question;

import java.time.LocalDate;
import java.util.UUID;

public record QuestionListSimpleCollectionDTO(
        UUID questionId,
        String title,
        LocalDate createdAt,
        boolean solutioned
) {}
