package com.memoria.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LinkDeviceRequest(
        @NotBlank String code,
        @NotBlank String deviceIdentifier,
        String deviceName) {
}

