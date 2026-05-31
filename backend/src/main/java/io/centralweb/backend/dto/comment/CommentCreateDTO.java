package io.centralweb.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentCreateDTO(
        @NotBlank(message = "O conteúdo não pode ser vazio")
        String content
) {}
