package io.centralweb.backend.dto.tag;

import jakarta.validation.constraints.NotBlank;

public record TagDTO(
        @NotBlank String technologyName,
        @NotBlank String color
) {}
