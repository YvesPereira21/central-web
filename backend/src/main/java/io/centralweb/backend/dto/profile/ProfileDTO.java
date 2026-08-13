package io.centralweb.backend.dto.profile;

import io.centralweb.backend.dto.qualification.QualificationDTO;

import java.util.List;
import java.util.UUID;

public record ProfileDTO(
        UUID profileId,
        UUID userId,
        String name,
        String bio,
        String level,
        long reputationScore,
        boolean professional,
        Long articlesCreatedByProfile,
        Long answersAccepted,
        String photoUrl,
        List<QualificationDTO> qualifications
) {}
