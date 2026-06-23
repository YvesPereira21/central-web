package io.centralweb.backend.dto.qualification;

import io.centralweb.backend.model.enums.ExperienceLevel;

import java.time.LocalDate;

public record QualificationDTO(
        String jobTitle,
        ExperienceLevel experienceLevel,
        String institution,
        LocalDate startDate,
        LocalDate endDate
) {}

