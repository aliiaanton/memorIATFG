package com.memoria.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PatientMessageRequest(
        @NotBlank String text) {
}

