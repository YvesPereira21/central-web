package io.centralweb.backend.dto.tag;

public record TagUpdateDTO(
        String technologyName,
        String languageLogoUrl,
        String color
) {}
