package io.centralweb.backend.events;

import java.util.UUID;

public record AnswerAcceptedEvent(
    UUID profileId
) {}
