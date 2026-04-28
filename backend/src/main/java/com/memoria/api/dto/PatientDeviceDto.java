package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientDeviceDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        String deviceIdentifier,
        String deviceName,
        OffsetDateTime linkedAt,
        OffsetDateTime revokedAt) {
}

