package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DangerousTopicDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        String term,
        String redirectHint,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

