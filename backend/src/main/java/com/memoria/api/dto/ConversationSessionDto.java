package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationSessionDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime lastEventAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

