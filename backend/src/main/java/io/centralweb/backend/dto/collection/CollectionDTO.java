package io.centralweb.backend.dto.collection;

import io.centralweb.backend.dto.article.ArticleDTO;
import io.centralweb.backend.dto.question.QuestionDTO;

import java.util.List;
import java.util.UUID;

public record CollectionDTO(
        UUID collectionId,
        String name,
        List<ArticleDTO> articles,
        List<QuestionDTO> questions
) {}
