package com.memoria.api.dto;

import java.time.OffsetDateTime;

public record ApiError(
        int status,
        String message,
        OffsetDateTime timestamp) {
}

