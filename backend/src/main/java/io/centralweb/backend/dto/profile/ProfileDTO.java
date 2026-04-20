package io.centralweb.backend.dto.profile;

import java.util.UUID;

public record ProfileDTO(
        UUID profileId,
        String bio,
        String username,
        String photoUrl,
        String expertise,
        String level,
        long reputationScore,
        boolean professional
) {}
