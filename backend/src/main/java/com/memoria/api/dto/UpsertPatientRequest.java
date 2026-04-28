package com.memoria.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record UpsertPatientRequest(
        @NotBlank String fullName,
        String preferredName,
        Integer birthYear,
        String relationship,
        String notes,
        String textSize,
        @DecimalMin("0.50") @DecimalMax("1.50") Double ttsSpeed) {
}

