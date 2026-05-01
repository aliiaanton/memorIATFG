package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CaregiverProfileDto(
        UUID id,
        UUID authUserId,
        String fullName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
