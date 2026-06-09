package io.centralweb.backend.events;

import io.centralweb.backend.enums.ExperienceLevel;

import java.util.UUID;

public record QualificationDeleteEvent(
        UUID profileId,
        ExperienceLevel experienceLevel
) {}
