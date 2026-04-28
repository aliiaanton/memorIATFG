package com.memoria.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.memoria.ai.AiClient;
import com.memoria.ai.AiPromptRequest;
import com.memoria.ai.SafeMemoryPrompt;
import com.memoria.api.dto.ConversationMessageDto;
import com.memoria.api.dto.ConversationResponseDto;
import com.memoria.api.dto.ConversationSessionDto;
import com.memoria.api.dto.DangerousTopicDto;
import com.memoria.api.dto.LoopRuleDto;
import com.memoria.api.dto.PatientDto;
import com.memoria.api.dto.PatientMessageRequest;
import com.memoria.api.dto.SafeMemoryDto;

@Service
public class ConversationService {

    private final MemoriaStore store;
    private final AiClient aiClient;

    public ConversationService(MemoriaStore store, AiClient aiClient) {
        this.store = store;
        this.aiClient = aiClient;
    }

    public ConversationResponseDto handlePatientMessage(String caregiverId, UUID sessionId,
            PatientMessageRequest request) {
        ConversationSessionDto session = store.getSession(caregiverId, sessionId);
        if (!"active".equals(session.status())) {
            throw new IllegalStateException("Session must be active to process messages");
        }

        UUID patientId = session.patientId();
        PatientDto patient = store.getPatient(caregiverId, patientId);
        ConversationMessageDto patientMessage = store.addMessage(caregiverId, patientId, sessionId, "patient",
                request.text());

        DangerousTopicDto dangerousTopic = findDangerousTopic(caregiverId, patientId, request.text());
        if (dangerousTopic != null) {
            String response = dangerousTopicResponse(caregiverId, patientId, patient, dangerousTopic);
            ConversationMessageDto responseMessage = store.addMessage(caregiverId, patientId, sessionId, "rule",
                    response);
            store.addEvent(caregiverId, patientId, sessionId, "dangerous_topic_detected",
                    "Tema delicado detectado: " + dangerousTopic.term(), Map.of("term", dangerousTopic.term()));
            store.addAlert(caregiverId, patientId, sessionId, "dangerous_topic", "warning",
                    "Tema delicado detectado", "El paciente ha mencionado: " + dangerousTopic.term());
            return new ConversationResponseDto(sessionId, patientMessage.id(), responseMessage.id(), response,
                    "dangerous_topic", true);
        }

        LoopRuleDto loopRule = findLoopRule(caregiverId, patientId, request.text());
        if (loopRule != null) {
            ConversationMessageDto responseMessage = store.addMessage(caregiverId, patientId, sessionId, "rule",
                    loopRule.answer());
            store.addEvent(caregiverId, patientId, sessionId, "loop_detected",
                    "Pregunta repetitiva detectada", Map.of("ruleId", loopRule.id().toString()));
            store.addAlert(caregiverId, patientId, sessionId, "loop_detected", "info",
                    "Pregunta repetitiva", "Se ha respondido con una regla configurada.");
            return new ConversationResponseDto(sessionId, patientMessage.id(), responseMessage.id(),
                    loopRule.answer(), "loop_rule", true);
        }

        String aiResponse = aiClient.generateResponse(buildAiPrompt(caregiverId, patientId, patient, request.text()));
        ConversationMessageDto responseMessage = store.addMessage(caregiverId, patientId, sessionId, "ai", aiResponse);
        store.addEvent(caregiverId, patientId, sessionId, "ai_response",
                "Respuesta generada por IA", Map.of());
        return new ConversationResponseDto(sessionId, patientMessage.id(), responseMessage.id(), aiResponse, "ai",
                false);
    }

    private DangerousTopicDto findDangerousTopic(String caregiverId, UUID patientId, String text) {
        String normalizedText = normalize(text);
        return store.listDangerousTopics(caregiverId, patientId).stream()
                .filter(DangerousTopicDto::active)
                .filter(topic -> normalizedText.contains(normalize(topic.term())))
                .findFirst()
                .orElse(null);
    }

    private LoopRuleDto findLoopRule(String caregiverId, UUID patientId, String text) {
        String normalizedText = normalize(text);
        return store.listLoopRules(caregiverId, patientId).stream()
                .filter(LoopRuleDto::active)
                .filter(rule -> matchesRule(normalizedText, normalize(rule.question())))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesRule(String normalizedText, String normalizedQuestion) {
        if (normalizedText.isBlank() || normalizedQuestion.isBlank()) {
            return false;
        }
        return normalizedText.contains(normalizedQuestion) || normalizedQuestion.contains(normalizedText);
    }

    private String dangerousTopicResponse(String caregiverId, UUID patientId, PatientDto patient,
            DangerousTopicDto dangerousTopic) {
        List<SafeMemoryDto> memories = store.listSafeMemories(caregiverId, patientId).stream()
                .filter(SafeMemoryDto::active)
                .toList();
        String name = patient.preferredName() == null || patient.preferredName().isBlank()
                ? patient.fullName()
                : patient.preferredName();

        if (dangerousTopic.redirectHint() != null && !dangerousTopic.redirectHint().isBlank()) {
            return "Te entiendo, " + name + ". Mejor pensemos en algo tranquilo. "
                    + dangerousTopic.redirectHint();
        }
        if (!memories.isEmpty()) {
            SafeMemoryDto memory = memories.get(0);
            return "Te entiendo, " + name + ". Vamos a hablar de algo agradable: " + memory.title() + ".";
        }
        return "Te entiendo, " + name + ". Vamos a hablar de algo tranquilo y agradable.";
    }

    private AiPromptRequest buildAiPrompt(String caregiverId, UUID patientId, PatientDto patient, String text) {
        List<SafeMemoryPrompt> memories = store.listSafeMemories(caregiverId, patientId).stream()
                .filter(SafeMemoryDto::active)
                .map(memory -> new SafeMemoryPrompt(memory.title(), memory.content()))
                .toList();
        List<String> dangerousTerms = store.listDangerousTopics(caregiverId, patientId).stream()
                .filter(DangerousTopicDto::active)
                .map(DangerousTopicDto::term)
                .toList();
        String name = patient.preferredName() == null || patient.preferredName().isBlank()
                ? patient.fullName()
                : patient.preferredName();
        return new AiPromptRequest(name, patient.notes(), text, memories, dangerousTerms);
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return normalized.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }
}
