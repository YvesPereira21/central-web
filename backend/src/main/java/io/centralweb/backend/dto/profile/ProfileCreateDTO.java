package io.centralweb.backend.dto.profile;

import io.centralweb.backend.dto.user.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileCreateDTO(
        @NotBlank String bio,
        @NotBlank String photoUrl,
        @NotBlank String expertise,
        @NotNull UserDTO user
) {}
