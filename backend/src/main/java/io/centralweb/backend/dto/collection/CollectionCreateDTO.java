package io.centralweb.backend.dto.collection;

import jakarta.validation.constraints.NotBlank;

public record CollectionCreateDTO(
        @NotBlank(message = "O nome da coleção não pode ser vazio")
        String name
) {}
