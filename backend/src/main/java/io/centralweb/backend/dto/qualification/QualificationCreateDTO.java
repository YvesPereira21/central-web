package io.centralweb.backend.dto.qualification;

import io.centralweb.backend.enums.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record QualificationCreateDTO(
        @NotBlank String jobTitle,
        @NotNull ExperienceLevel experienceLevel,
        @NotBlank String institution,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
