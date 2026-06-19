package io.centralweb.backend.dto.question;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record QuestionUpdateDTO(
        String title,
        String content,
        @Size(max = 7) Set<String> technologyNames
) {}
