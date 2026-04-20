package io.centralweb.backend.dto.answer;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AnswerAcceptedDTO(
        @NotNull UUID answerId,
        @NotNull boolean accepted
) {}

