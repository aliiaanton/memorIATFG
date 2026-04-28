package com.memoria.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationMessageDto(
        UUID id,
        String caregiverId,
        UUID patientId,
        UUID sessionId,
        String sender,
        String content,
        OffsetDateTime createdAt) {
}

