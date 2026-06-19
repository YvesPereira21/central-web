package io.centralweb.backend.dto.question;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record QuestionDTO(
        UUID questionId,
        String title,
        String content,
        boolean published,
        boolean solutioned,
        LocalDate createdAt,
        ProfileSimpleDTO profile,
        Set<TagDTO> tags,
        Set<AnswerDTO> answers,
        Long questionTotalLikes,
        boolean liked,
        boolean saved
) {}
