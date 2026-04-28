package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record SessionEventDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        UUID sessionId,
        String eventType,
        String description,
        Map<String, Object> metadata,
        OffsetDateTime createdAt) {
}

