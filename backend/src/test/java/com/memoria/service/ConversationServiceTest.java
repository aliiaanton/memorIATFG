package com.memoria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.memoria.ai.AiClient;
import com.memoria.ai.AiPromptRequest;
import com.memoria.api.dto.PatientDto;
import com.memoria.api.dto.PatientMessageRequest;
import com.memoria.api.dto.UpsertDangerousTopicRequest;
import com.memoria.api.dto.UpsertLoopRuleRequest;
import com.memoria.api.dto.UpsertPatientRequest;
import com.memoria.api.dto.UpsertSafeMemoryRequest;
import com.memoria.cognitive.CognitiveStimulationService;

class ConversationServiceTest {

    private static final String CAREGIVER_ID = "caregiver-test";

    private InMemoryStore store;
    private StubAiClient aiClient;
    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        aiClient = new StubAiClient();
        conversationService = new ConversationService(store, aiClient, new CognitiveStimulationService());
    }

    @Test
    void dangerousTopicTakesPriorityAndCreatesAlert() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        store.createDangerousTopic(CAREGIVER_ID, patient.id(),
                new UpsertDangerousTopicRequest("dinero", "Podemos hablar de costura.", true));
        store.createLoopRule(CAREGIVER_ID, patient.id(),
                new UpsertLoopRuleRequest("dinero", "El dinero esta guardado.", true));

        var response = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Me preocupa el dinero"));

        assertThat(response.source()).isEqualTo("dangerous_topic");
        assertThat(response.alertCreated()).isTrue();
        assertThat(response.responseText()).contains("Podemos hablar de costura.");
        assertThat(store.listAlerts(CAREGIVER_ID))
                .singleElement()
                .satisfies(alert -> assertThat(alert.alertType()).isEqualTo("dangerous_topic"));
        assertThat(store.listEvents(CAREGIVER_ID, sessionId))
                .anySatisfy(event -> assertThat(event.eventType()).isEqualTo("dangerous_topic_detected"));
        assertThat(aiClient.calls()).isZero();
    }

    @Test
    void loopRuleRespondsWithoutCallingAiService() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        store.createLoopRule(CAREGIVER_ID, patient.id(),
                new UpsertLoopRuleRequest("Donde esta Maria", "Maria esta cerca, no te preocupes.", true));

        var response = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Donde esta Maria?"));

        assertThat(response.source()).isEqualTo("loop_rule");
        assertThat(response.alertCreated()).isTrue();
        assertThat(response.responseText()).isEqualTo("Maria esta cerca, no te preocupes.");
        assertThat(store.listMessages(CAREGIVER_ID, sessionId))
                .extracting(message -> message.sender())
                .containsExactlyInAnyOrder("patient", "rule");
        assertThat(aiClient.calls()).isZero();
    }

    @Test
    void aiResponseIsUsedWhenNoConfiguredRuleMatches() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        store.createSafeMemory(CAREGIVER_ID, patient.id(),
                new UpsertSafeMemoryRequest("Costura", "Le gustaba coser por las tardes.", true));

        var response = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Hola, quiero hablar un rato"));

        assertThat(response.source()).isEqualTo("ai");
        assertThat(response.alertCreated()).isFalse();
        assertThat(response.responseText()).isEqualTo("Respuesta tranquila de prueba.");
        assertThat(store.listMessages(CAREGIVER_ID, sessionId))
                .extracting(message -> message.sender())
                .containsExactlyInAnyOrder("patient", "ai");
        assertThat(aiClient.calls()).isEqualTo(1);
    }

    @Test
    void aiPromptFiltersBlankContextBeforeCallingAiService() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        store.createSafeMemory(CAREGIVER_ID, patient.id(),
                new UpsertSafeMemoryRequest("   ", "No deberia llegar al microservicio.", true));
        store.createSafeMemory(CAREGIVER_ID, patient.id(),
                new UpsertSafeMemoryRequest("Costura", "  Le gustaba coser por las tardes.  ", true));
        store.createDangerousTopic(CAREGIVER_ID, patient.id(),
                new UpsertDangerousTopicRequest("   ", "Sin termino valido.", true));
        store.createDangerousTopic(CAREGIVER_ID, patient.id(),
                new UpsertDangerousTopicRequest(" hospital ", "Hablar de plantas.", true));

        conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Hola, quiero hablar un rato"));

        assertThat(aiClient.lastRequest().safeMemories())
                .singleElement()
                .satisfies(memory -> {
                    assertThat(memory.title()).isEqualTo("Costura");
                    assertThat(memory.content()).isEqualTo("Le gustaba coser por las tardes.");
                });
        assertThat(aiClient.lastRequest().dangerousTerms()).containsExactly("hospital");
    }

    @Test
    void positiveMoodStartsCognitiveStimulationGame() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());

        var response = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Estoy tranquila y bien"));

        assertThat(response.source()).isEqualTo("cognitive_stimulation");
        assertThat(response.responseText()).isEqualTo("Completemos un refran: A quien madruga...");
        assertThat(aiClient.calls()).isZero();
        assertThat(store.listEvents(CAREGIVER_ID, sessionId))
                .anySatisfy(event -> {
                    assertThat(event.eventType()).isEqualTo("cognitive_stimulation");
                    assertThat(event.metadata()).containsEntry("trigger", "STIMULATION");
                    assertThat(event.metadata()).containsEntry("gameType", "COMPLETE_PROVERB");
                });
    }

    @Test
    void activeGameUsesErrorlessLearningWhenAnswerDoesNotMatch() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Estoy tranquila y bien"));

        var response = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("patata"));

        assertThat(response.source()).isEqualTo("cognitive_stimulation");
        assertThat(response.responseText()).isEqualTo("¡Casi! Yo pensaba en: Dios le ayuda.");
        assertThat(response.responseText().toLowerCase()).doesNotContain("incorrecto", "equivocaste");
        assertThat(store.listMessages(CAREGIVER_ID, sessionId))
                .extracting(message -> message.sender())
                .containsExactlyInAnyOrder("patient", "patient", "cognitive", "cognitive");
    }

    @Test
    void repeatedIdeaStartsMusicalCircuitBreakerBeforeLoopRules() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        store.createLoopRule(CAREGIVER_ID, patient.id(),
                new UpsertLoopRuleRequest("quiero mi chaqueta", "La chaqueta esta guardada.", true));

        var firstResponse = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("quiero mi chaqueta"));
        var secondResponse = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("quiero mi chaqueta"));

        assertThat(firstResponse.source()).isEqualTo("loop_rule");
        assertThat(secondResponse.source()).isEqualTo("cognitive_stimulation");
        assertThat(secondResponse.responseText()).isEqualTo("Cantemos: Que llueva, que llueva...");
    }

    @Test
    void prolongedSilenceCompletesActiveGameGently() {
        PatientDto patient = createPatient();
        UUID sessionId = createActiveSession(patient.id());
        conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest("Estoy tranquila y bien"));

        var response = conversationService.handlePatientMessage(CAREGIVER_ID, sessionId,
                new PatientMessageRequest(CognitiveStimulationService.SILENCE_MARKER));

        assertThat(response.source()).isEqualTo("cognitive_stimulation");
        assertThat(response.responseText()).isEqualTo("¡Casi! Yo pensaba en: Dios le ayuda.");
    }

    @Test
    void inactiveSessionRejectsPatientMessages() {
        PatientDto patient = createPatient();
        UUID waitingSessionId = store.createSession(CAREGIVER_ID, patient.id()).id();

        assertThatThrownBy(() -> conversationService.handlePatientMessage(CAREGIVER_ID, waitingSessionId,
                new PatientMessageRequest("Hola")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("active");
    }

    private PatientDto createPatient() {
        return store.createPatient(CAREGIVER_ID,
                new UpsertPatientRequest("Maria Lopez", "Maria", 1942, "madre",
                        "Le gustan las plantas y la costura.", "large", 1.0));
    }

    private UUID createActiveSession(UUID patientId) {
        UUID sessionId = store.createSession(CAREGIVER_ID, patientId).id();
        return store.updateSessionStatus(CAREGIVER_ID, sessionId, "active").id();
    }

    private static final class StubAiClient extends AiClient {
        private int calls;
        private AiPromptRequest lastRequest;

        private StubAiClient() {
            super(RestClient.builder(), "http://localhost:1");
        }

        @Override
        public String generateResponse(AiPromptRequest request) {
            calls++;
            lastRequest = request;
            return "Respuesta tranquila de prueba.";
        }

        int calls() {
            return calls;
        }

        AiPromptRequest lastRequest() {
            return lastRequest;
        }
    }
}
