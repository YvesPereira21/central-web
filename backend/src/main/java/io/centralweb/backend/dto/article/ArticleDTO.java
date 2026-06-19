package io.centralweb.backend.dto.article;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ArticleDTO(
        UUID articleId,
        String title,
        String content,
        LocalDate createdAt,
        Set<TagDTO> tags,
        ProfileSimpleDTO profile,
        Long articleTotalLikes,
        boolean liked,
        boolean saved
) {}
