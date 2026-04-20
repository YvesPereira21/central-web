package io.centralweb.backend.dto.question;

import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.answer.AnswerDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuestionDTO(
        UUID questionId,
        String title,
        String content,
        boolean published,
        boolean solutioned,
        LocalDate createdAt,
        ProfileSimpleDTO profile,
        List<TagDTO> tags,
        List<AnswerDTO> answers,
        Long questionTotalLikes
) {}

