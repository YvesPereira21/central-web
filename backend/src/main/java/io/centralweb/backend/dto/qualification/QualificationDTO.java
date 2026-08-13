package io.centralweb.backend.dto.qualification;

import io.centralweb.backend.model.enums.ExperienceLevel;

import java.time.LocalDate;
import java.util.UUID;

public record QualificationDTO(
        UUID qualificationId,
        String jobTitle,
        ExperienceLevel experienceLevel,
        String institution,
        LocalDate startDate,
        LocalDate endDate,
        boolean verified,
        UUID profileId,
        String profileName
) {}
