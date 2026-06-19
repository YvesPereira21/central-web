package io.centralweb.backend.dto.article;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record ArticleUpdateDTO(
        String title,
        String content,
        @Size(max = 7) Set<String> technologyNames
) {}

