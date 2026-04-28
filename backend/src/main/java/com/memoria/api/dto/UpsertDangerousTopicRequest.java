package com.memoria.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertDangerousTopicRequest(
        @NotBlank String term,
        String redirectHint,
        Boolean active) {
}

