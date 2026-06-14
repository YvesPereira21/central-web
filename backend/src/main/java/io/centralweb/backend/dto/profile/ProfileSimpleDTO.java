package io.centralweb.backend.dto.profile;

import java.util.UUID;

public record ProfileSimpleDTO(
        UUID profileId,
        UUID userId,
        String name,
        String level,
        boolean professional,
        String photoUrl
) {}
