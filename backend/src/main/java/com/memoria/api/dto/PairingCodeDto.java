package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PairingCodeDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        String code,
        OffsetDateTime expiresAt,
        OffsetDateTime consumedAt,
        OffsetDateTime createdAt) {
}

