package io.centralweb.backend.dto.profile;

import java.util.UUID;

public record ProfileDTO(
        UUID profileId,
        String name,
        String bio,
        String expertise,
        String level,
        long reputationScore,
        boolean professional
) {}
