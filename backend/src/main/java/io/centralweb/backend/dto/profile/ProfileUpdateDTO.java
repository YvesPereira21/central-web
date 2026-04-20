package io.centralweb.backend.dto.profile;

public record ProfileUpdateDTO(
        String bio,
        String photoUrl,
        String expertise
) {}

