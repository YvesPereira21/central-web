package io.centralweb.backend.dto.comment;

import io.centralweb.backend.dto.profile.ProfileSimpleDTO;

import java.time.LocalDate;
import java.util.UUID;

public record CommentDTO(
        UUID commentId,
        String content,
        LocalDate createdAt,
        ProfileSimpleDTO profile
) {}
