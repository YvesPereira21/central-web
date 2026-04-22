package io.centralweb.backend.dto.profile;

import java.util.UUID;

public record ProfileSimpleDTO(
        UUID profileId,
        String name,
        String expertise,
        String level,
        boolean professional
) {}
