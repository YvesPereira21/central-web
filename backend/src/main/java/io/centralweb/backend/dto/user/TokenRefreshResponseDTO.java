package io.centralweb.backend.dto.user;

public record TokenRefreshResponseDTO(
        String accessToken,
        String refreshToken
) {}
