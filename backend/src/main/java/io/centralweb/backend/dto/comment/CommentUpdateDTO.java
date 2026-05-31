package io.centralweb.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateDTO(
        @NotBlank(message = "O conteúdo não pode ser vazio")
        String content
) {}
