package io.centralweb.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequestDTO(
        @NotBlank String refreshToken
) {}
