package io.centralweb.backend.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ArticleCreateDTO(
        @NotBlank String title,
        @NotBlank String content,
        @NotEmpty List<String> technologyNames
) {}

