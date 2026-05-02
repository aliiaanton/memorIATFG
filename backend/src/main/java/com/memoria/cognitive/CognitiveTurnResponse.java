package com.memoria.cognitive;

public record CognitiveTurnResponse(
        String responseText,
        CognitiveGameType gameType,
        CognitiveTrigger trigger,
        boolean startedGame,
        boolean completedGame) {
}
