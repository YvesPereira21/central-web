package io.centralweb.backend.event;

import java.util.UUID;

public record QuestionDeleteEvent(
        UUID profileId
) {}
