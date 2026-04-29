package io.centralweb.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserDTO(
        @Email(
                regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Formato de e-mail inválido"
        ) String email,
        @NotBlank String password
) {}
