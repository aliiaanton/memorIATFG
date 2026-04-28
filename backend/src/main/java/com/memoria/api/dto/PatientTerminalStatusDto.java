package com.memoria.api.dto;

import java.util.UUID;

public record PatientTerminalStatusDto(
        boolean linked,
        UUID patientId,
        String patientName,
        UUID sessionId,
        String sessionStatus) {
}

