package io.centralweb.backend.dto.question;


import io.centralweb.backend.dto.tag.TagDTO;
import io.centralweb.backend.dto.profile.ProfileSimpleDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record QuestionListDTO(
        UUID questionId,
        String title,
        String content,
        boolean published,
        boolean solutioned,
        LocalDate createdAt,
        ProfileSimpleDTO profile,
        List<TagDTO> tags,
        Long questionTotalLikes
) {}
