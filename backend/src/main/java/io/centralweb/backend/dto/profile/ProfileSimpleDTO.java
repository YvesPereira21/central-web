package io.centralweb.backend.dto.profile;

import java.util.UUID;

public record ProfileSimpleDTO(
        UUID profileId,
        String username,
        String photoUrl,
        String expertise,
        String level,
        boolean professional
) {}
