package io.centralweb.backend.enums;

public enum UserRole {
    ADMIN("Admin"),
    PERSON("Pessoa");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}

