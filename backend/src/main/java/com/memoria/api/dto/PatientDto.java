package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientDto(
        UUID id,
        String caregiverId,
        String fullName,
        String preferredName,
        Integer birthYear,
        String relationship,
        String notes,
        String textSize,
        Double ttsSpeed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

