package com.memoria.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertLoopRuleRequest(
        @NotBlank String question,
        @NotBlank String answer,
        Boolean active) {
}

