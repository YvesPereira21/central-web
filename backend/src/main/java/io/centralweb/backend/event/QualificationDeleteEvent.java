package io.centralweb.backend.event;

import io.centralweb.backend.model.enums.ExperienceLevel;

import java.util.UUID;

public record QualificationDeleteEvent(
        UUID profileId,
        ExperienceLevel experienceLevel
) {}
