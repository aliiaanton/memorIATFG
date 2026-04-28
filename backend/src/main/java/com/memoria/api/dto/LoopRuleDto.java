package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LoopRuleDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        String question,
        String answer,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

