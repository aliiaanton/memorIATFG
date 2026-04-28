package com.memoria.ai;

public record AiPromptResponse(
        String text,
        String provider,
        String model) {
}

