package io.centralweb.backend.dto.profile;

import java.util.UUID;

public record ProfileDTO(
        UUID profileId,
        UUID userId,
        String name,
        String bio,
        String level,
        long reputationScore,
        boolean professional
) {}
