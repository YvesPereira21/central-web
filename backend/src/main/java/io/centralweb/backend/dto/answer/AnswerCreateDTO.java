package io.centralweb.backend.dto.answer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AnswerCreateDTO(
        @NotBlank String content,
        @NotNull UUID questionId
) {}

