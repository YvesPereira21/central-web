package io.centralweb.backend.dto.question;

import java.util.List;

public record QuestionUpdateDTO(
        String title,
        String content,
        List<String> technologyNames
) {}
