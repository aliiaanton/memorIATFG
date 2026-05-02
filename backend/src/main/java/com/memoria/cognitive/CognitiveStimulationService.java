package com.memoria.cognitive;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.memoria.api.dto.ConversationMessageDto;

@Service
public class CognitiveStimulationService {

    public static final String SILENCE_MARKER = "[silencio prolongado]";

    private static final List<String> POSITIVE_OR_CALM_TERMS = List.of(
            "bien",
            "contento",
            "contenta",
            "feliz",
            "alegre",
            "tranquilo",
            "tranquila",
            "calmado",
            "calmada",
            "aburrido",
            "aburrida",
            "me aburro",
            "que hago");

    private static final List<String> ANXIETY_OR_FRUSTRATION_TERMS = List.of(
            "miedo",
            "ansiedad",
            "nervioso",
            "nerviosa",
            "preocupado",
            "preocupada",
            "frustrado",
            "frustrada",
            "enfadado",
            "enfadada",
            "agobiado",
            "agobiada",
            "quiero irme",
            "donde esta",
            "donde estan",
            "me quiero ir");

    private final ConcurrentMap<UUID, SessionState> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger stimulationCursor = new AtomicInteger();

    public void clearSession(UUID sessionId) {
        sessions.remove(sessionId);
    }

    public Optional<CognitiveTurnResponse> handleActiveGame(UUID sessionId, String patientText) {
        SessionState state = sessions.remove(sessionId);
        if (state == null) {
            return Optional.empty();
        }

        Challenge challenge = state.challenge();
        String normalized = normalize(patientText);
        boolean silence = isSilence(patientText);
        boolean accepted = !silence && (challenge.acceptsAnyAnswer() || challenge.matches(normalized));
        String response = accepted ? challenge.successPrompt() : challenge.assistedPrompt();

        return Optional.of(new CognitiveTurnResponse(
                response,
                challenge.gameType(),
                state.trigger(),
                false,
                true));
    }

    public Optional<CognitiveTurnResponse> handleSilenceWithoutActiveGame(String patientText) {
        if (!isSilence(patientText)) {
            return Optional.empty();
        }
        return Optional.of(new CognitiveTurnResponse(
                "Estoy aqui contigo. Tomate tu tiempo.",
                null,
                CognitiveTrigger.SILENCE_SUPPORT,
                false,
                false));
    }

    public Optional<CognitiveTurnResponse> maybeStartCircuitBreaker(UUID sessionId, String patientText,
            List<ConversationMessageDto> previousMessages) {
        if (isSilence(patientText)) {
            return Optional.empty();
        }
        if (!isAnxiousOrFrustrated(patientText) && !isRepetitionLoop(patientText, previousMessages)) {
            return Optional.empty();
        }
        Challenge challenge = songChallenge();
        sessions.put(sessionId, new SessionState(challenge, CognitiveTrigger.CIRCUIT_BREAKER, OffsetDateTime.now()));
        return Optional.of(new CognitiveTurnResponse(
                challenge.prompt(),
                challenge.gameType(),
                CognitiveTrigger.CIRCUIT_BREAKER,
                true,
                false));
    }

    public Optional<CognitiveTurnResponse> maybeStartStimulation(UUID sessionId, String patientText) {
        if (isSilence(patientText) || !isPositiveCalmOrBored(patientText)) {
            return Optional.empty();
        }

        Challenge challenge = nextStimulationChallenge();
        sessions.put(sessionId, new SessionState(challenge, CognitiveTrigger.STIMULATION, OffsetDateTime.now()));
        return Optional.of(new CognitiveTurnResponse(
                challenge.prompt(),
                challenge.gameType(),
                CognitiveTrigger.STIMULATION,
                true,
                false));
    }

    public boolean isRepetitionLoop(String patientText, List<ConversationMessageDto> previousMessages) {
        String current = normalize(patientText);
        if (current.length() < 8) {
            return false;
        }

        return previousMessages.stream()
                .filter(message -> "patient".equals(message.sender()))
                .map(ConversationMessageDto::content)
                .map(this::normalize)
                .filter(previous -> previous.length() >= 8)
                .skip(Math.max(0, previousPatientMessageCount(previousMessages) - 5))
                .anyMatch(previous -> isSimilarIdea(current, previous));
    }

    private int previousPatientMessageCount(List<ConversationMessageDto> previousMessages) {
        return (int) previousMessages.stream()
                .filter(message -> "patient".equals(message.sender()))
                .count();
    }

    private boolean isPositiveCalmOrBored(String text) {
        String normalized = normalize(text);
        return POSITIVE_OR_CALM_TERMS.stream().anyMatch(normalized::contains);
    }

    private boolean isAnxiousOrFrustrated(String text) {
        String normalized = normalize(text);
        return ANXIETY_OR_FRUSTRATION_TERMS.stream().anyMatch(normalized::contains);
    }

    private boolean isSilence(String text) {
        return normalize(text).equals(normalize(SILENCE_MARKER));
    }

    private Challenge nextStimulationChallenge() {
        int index = Math.floorMod(stimulationCursor.getAndIncrement(), 3);
        return switch (index) {
            case 0 -> proverbChallenge();
            case 1 -> categoryChallenge();
            default -> oppositeChallenge();
        };
    }

    private Challenge songChallenge() {
        return new Challenge(
                CognitiveGameType.COMPLETE_SONG,
                "Cantemos: Que llueva, que llueva...",
                List.of("la virgen de la cueva", "la cueva"),
                false,
                "¡Exacto! Que buena memoria musical.",
                "¡Casi! Yo pensaba en: la Virgen de la Cueva.");
    }

    private Challenge proverbChallenge() {
        return new Challenge(
                CognitiveGameType.COMPLETE_PROVERB,
                "Completemos un refran: A quien madruga...",
                List.of("dios le ayuda", "le ayuda"),
                false,
                "¡Exacto! Que buena memoria.",
                "¡Casi! Yo pensaba en: Dios le ayuda.");
    }

    private Challenge categoryChallenge() {
        return new Challenge(
                CognitiveGameType.CATEGORY_BAG,
                "Vamos de compra. Dime algo para la cesta.",
                List.of("pan"),
                true,
                "Muy bien. Eso nos sirve para la compra.",
                "Muy bien, yo pondria pan en la cesta.");
    }

    private Challenge oppositeChallenge() {
        return new Challenge(
                CognitiveGameType.OPPOSITES,
                "Dime el contrario de blanco.",
                List.of("negro"),
                false,
                "¡Exacto! Blanco y negro, muy bien.",
                "¡Casi! Yo pensaba en negro.");
    }

    private boolean isSimilarIdea(String current, String previous) {
        if (current.equals(previous)) {
            return true;
        }
        if (current.contains(previous) || previous.contains(current)) {
            return Math.min(current.length(), previous.length()) >= 10;
        }
        return jaccard(current, previous) >= 0.72;
    }

    private double jaccard(String left, String right) {
        List<String> leftWords = words(left);
        List<String> rightWords = words(right);
        if (leftWords.isEmpty() || rightWords.isEmpty()) {
            return 0.0;
        }

        long intersection = leftWords.stream()
                .filter(rightWords::contains)
                .distinct()
                .count();
        long union = Stream.concat(leftWords.stream(), rightWords.stream())
                .distinct()
                .count();
        return union == 0 ? 0.0 : (double) intersection / union;
    }

    private List<String> words(String text) {
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return List.of();
        }
        return new ArrayList<>(List.of(normalized.split(" ")));
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return normalized.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private record SessionState(
            Challenge challenge,
            CognitiveTrigger trigger,
            OffsetDateTime startedAt) {
    }

    private record Challenge(
            CognitiveGameType gameType,
            String prompt,
            List<String> expectedAnswers,
            boolean acceptsAnyAnswer,
            String successPrompt,
            String assistedPrompt) {

        boolean matches(String normalizedText) {
            return expectedAnswers.stream()
                    .map(answer -> Normalizer.normalize(answer, Normalizer.Form.NFD)
                            .replaceAll("\\p{M}", "")
                            .toLowerCase()
                            .replaceAll("[^a-z0-9 ]", " ")
                            .replaceAll("\\s+", " ")
                            .trim())
                    .anyMatch(answer -> normalizedText.contains(answer) || answer.contains(normalizedText));
        }
    }
}
