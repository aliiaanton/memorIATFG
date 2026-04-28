package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        UUID sessionId,
        String alertType,
        String severity,
        String title,
        String message,
        OffsetDateTime readAt,
        OffsetDateTime createdAt) {
}

