package io.centralweb.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
        @NotBlank String refreshToken
) {}
