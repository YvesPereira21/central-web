package io.centralweb.backend.exception;

public class QuestionHaveAnswerAlreadyAcceptedException extends RuntimeException {
    public QuestionHaveAnswerAlreadyAcceptedException(String message) {
        super(message);
    }
}
