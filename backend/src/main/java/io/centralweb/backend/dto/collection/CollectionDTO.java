package io.centralweb.backend.dto.collection;

import io.centralweb.backend.dto.article.ArticleListSimpleCollectionDTO;
import io.centralweb.backend.dto.question.QuestionListSimpleCollectionDTO;

import java.util.Set;
import java.util.UUID;

public record CollectionDTO(
        UUID collectionId,
        String name,
        Set<ArticleListSimpleCollectionDTO> articles,
        Set<QuestionListSimpleCollectionDTO> questions
) {}
