package io.centralweb.backend.dto.user;

public record LoginRequestDTO (
        String email,
        String password
){}
