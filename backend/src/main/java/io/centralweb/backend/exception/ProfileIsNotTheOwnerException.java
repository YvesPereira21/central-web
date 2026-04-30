package io.centralweb.backend.exception;

public class ProfileIsNotTheOwnerException extends RuntimeException {
    public ProfileIsNotTheOwnerException(String message) {
        super(message);
    }
}
