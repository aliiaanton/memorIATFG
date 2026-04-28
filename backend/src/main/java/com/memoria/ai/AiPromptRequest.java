package com.memoria.ai;

import java.util.List;

public record AiPromptRequest(
        String patientName,
        String patientNotes,
        String message,
        List<SafeMemoryPrompt> safeMemories,
        List<String> dangerousTerms) {
}

