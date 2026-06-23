package io.centralweb.backend.event;

import io.centralweb.backend.model.enums.ExperienceLevel;

import java.util.UUID;

public record QualificationCreateEvent(
        UUID profileId,
        ExperienceLevel experienceLevel
) {}
