package com.memoria.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertCaregiverProfileRequest(
        @NotBlank @Size(max = 120) String fullName) {
}
