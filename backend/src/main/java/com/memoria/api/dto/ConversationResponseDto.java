package com.memoria.api.dto;

import java.util.UUID;

public record ConversationResponseDto(
        UUID sessionId,
        UUID patientMessageId,
        UUID responseMessageId,
        String responseText,
        String source,
        boolean alertCreated) {
}

