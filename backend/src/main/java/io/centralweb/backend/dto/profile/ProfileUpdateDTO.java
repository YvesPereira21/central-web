package io.centralweb.backend.dto.profile;

public record ProfileUpdateDTO(
        String name,
        String bio,
        String expertise
) {}

