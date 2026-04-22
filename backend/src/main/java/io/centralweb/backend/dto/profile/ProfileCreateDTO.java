package io.centralweb.backend.dto.profile;

import io.centralweb.backend.dto.user.UserDTO;
import io.centralweb.backend.enums.ProfileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileCreateDTO(
        @NotBlank String name,
        @NotBlank String bio,
        @NotBlank ProfileType profileType,
        @NotBlank String expertise,
        @NotNull UserDTO user
) {}
