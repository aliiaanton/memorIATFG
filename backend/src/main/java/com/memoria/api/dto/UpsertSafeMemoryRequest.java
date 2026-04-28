package com.memoria.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertSafeMemoryRequest(
        @NotBlank String title,
        @NotBlank String content,
        Boolean active) {
}

