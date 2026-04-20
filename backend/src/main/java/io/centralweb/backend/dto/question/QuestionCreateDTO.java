package io.centralweb.backend.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuestionCreateDTO(
        @NotBlank String title,
        @NotBlank String content,
        @NotEmpty List<String> technologyNames
) {}
