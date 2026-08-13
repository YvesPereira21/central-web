package io.centralweb.backend.dto.auth;

import io.centralweb.backend.model.enums.UserRole;
import java.util.UUID;

public record AuthUserDTO(
        UUID userId,
        String email,
        UserRole role
) {}
