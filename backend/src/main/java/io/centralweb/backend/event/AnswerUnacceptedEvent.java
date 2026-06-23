package io.centralweb.backend.event;

import java.util.UUID;

public record AnswerUnacceptedEvent(
        UUID profileId
) {
}
