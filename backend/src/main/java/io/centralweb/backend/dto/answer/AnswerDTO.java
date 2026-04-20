package io.centralweb.backend.dto.answer;

import io.centralweb.backend.dto.profile.ProfileSimpleDTO;

import java.time.LocalDate;
import java.util.UUID;

public record AnswerDTO(
        UUID answerId,
        String content,
        boolean accepted,
        LocalDate createdAt,
        ProfileSimpleDTO profile,
        Long answerTotalLikes
) {}

