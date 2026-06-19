package io.centralweb.backend.dto.article;

import jakarta.validation.constraints.Size;

import java.util.List;

public record ArticleUpdateDTO(
        String title,
        String content,
        @Size(max = 7) List<String> technologyNames
) {}

