package io.centralweb.backend.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record QuestionCreateDTO(
        @NotBlank String title,
        @NotBlank String content,
        @NotEmpty @Size(max = 7) Set<String> technologyNames
) {}
