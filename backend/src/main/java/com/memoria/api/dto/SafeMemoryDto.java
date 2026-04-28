package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SafeMemoryDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        String title,
        String content,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

