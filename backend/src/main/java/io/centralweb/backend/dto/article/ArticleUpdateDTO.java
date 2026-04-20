package io.centralweb.backend.dto.article;

import java.util.List;

public record ArticleUpdateDTO(
        String title,
        String content,
        List<String> tags
) {}

