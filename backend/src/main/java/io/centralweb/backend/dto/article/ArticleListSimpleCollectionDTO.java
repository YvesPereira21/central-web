package io.centralweb.backend.dto.article;

import java.time.LocalDate;
import java.util.UUID;

public record ArticleListSimpleCollectionDTO(
        UUID articleId,
        String title,
        LocalDate createdAt
) {}
