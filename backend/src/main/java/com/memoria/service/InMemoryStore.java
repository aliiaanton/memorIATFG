package com.memoria.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.memoria.api.dto.AlertDto;
import com.memoria.api.dto.CaregiverProfileDto;
import com.memoria.api.dto.ConversationMessageDto;
import com.memoria.api.dto.ConversationSessionDto;
import com.memoria.api.dto.DangerousTopicDto;
import com.memoria.api.dto.LoopRuleDto;
import com.memoria.api.dto.PairingCodeDto;
import com.memoria.api.dto.PatientDeviceDto;
import com.memoria.api.dto.PatientDto;
import com.memoria.api.dto.SafeMemoryDto;
import com.memoria.api.dto.SessionEventDto;
import com.memoria.api.dto.UpsertCaregiverProfileRequest;
import com.memoria.api.dto.UpsertDangerousTopicRequest;
import com.memoria.api.dto.UpsertLoopRuleRequest;
import com.memoria.api.dto.UpsertPatientRequest;
import com.memoria.api.dto.UpsertSafeMemoryRequest;

@Service
@ConditionalOnProperty(name = "app.store", havingValue = "memory", matchIfMissing = true)
public class InMemoryStore implements MemoriaStore {

    private static final char[] PAIRING_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, CaregiverProfileDto> caregiverProfiles = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, PatientDto>> patients = new ConcurrentHashMap<>();
    private final Map<UUID, PairingCodeDto> pairingCodes = new ConcurrentHashMap<>();
    private final Map<String, PairingCodeDto> pairingCodesByCode = new ConcurrentHashMap<>();
    private final Map<UUID, PatientDeviceDto> patientDevices = new ConcurrentHashMap<>();
    private final Map<UUID, LoopRuleDto> loopRules = new ConcurrentHashMap<>();
    private final Map<UUID, DangerousTopicDto> dangerousTopics = new ConcurrentHashMap<>();
    private final Map<UUID, SafeMemoryDto> safeMemories = new ConcurrentHashMap<>();
    private final Map<UUID, ConversationSessionDto> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, ConversationMessageDto> messages = new ConcurrentHashMap<>();
    private final Map<UUID, SessionEventDto> events = new ConcurrentHashMap<>();
    private final Map<UUID, AlertDto> alerts = new ConcurrentHashMap<>();

    @Override
    public CaregiverProfileDto getOrCreateCaregiverProfile(String caregiverId, String fallbackFullName) {
        return caregiverProfiles.computeIfAbsent(caregiverId, id -> {
            OffsetDateTime now = OffsetDateTime.now();
            UUID profileId = UUID.fromString(id);
            return new CaregiverProfileDto(
                    profileId,
                    profileId,
                    defaultFullName(fallbackFullName),
                    now,
                    now);
        });
    }

    @Override
    public CaregiverProfileDto updateCaregiverProfile(String caregiverId, UpsertCaregiverProfileRequest request) {
        CaregiverProfileDto current = getOrCreateCaregiverProfile(caregiverId, request.fullName());
        CaregiverProfileDto updated = new CaregiverProfileDto(
                current.id(),
                current.authUserId(),
                defaultFullName(request.fullName()),
                current.createdAt(),
                OffsetDateTime.now());
        caregiverProfiles.put(caregiverId, updated);
        return updated;
    }

    @Override
    public List<PatientDto> listPatients(String caregiverId) {
        return patientsByCaregiver(caregiverId).values().stream()
                .sorted(Comparator.comparing(PatientDto::createdAt).reversed())
                .toList();
    }

    @Override
    public PatientDto createPatient(String caregiverId, UpsertPatientRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        PatientDto patient = new PatientDto(
                UUID.randomUUID(),
                caregiverId,
                request.fullName(),
                request.preferredName(),
                request.birthYear(),
                request.relationship(),
                request.notes(),
                defaultTextSize(request.textSize()),
                defaultTtsSpeed(request.ttsSpeed()),
                now,
                now);
        patientsByCaregiver(caregiverId).put(patient.id(), patient);
        return patient;
    }

    @Override
    public PatientDto getPatient(String caregiverId, UUID patientId) {
        PatientDto patient = patientsByCaregiver(caregiverId).get(patientId);
        if (patient == null) {
            throw new NotFoundException("Patient not found");
        }
        return patient;
    }

    @Override
    public PatientDto updatePatient(String caregiverId, UUID patientId, UpsertPatientRequest request) {
        PatientDto current = getPatient(caregiverId, patientId);
        PatientDto updated = new PatientDto(
                current.id(),
                current.caregiverId(),
                request.fullName(),
                request.preferredName(),
                request.birthYear(),
                request.relationship(),
                request.notes(),
                defaultTextSize(request.textSize()),
                defaultTtsSpeed(request.ttsSpeed()),
                current.createdAt(),
                OffsetDateTime.now());
        patientsByCaregiver(caregiverId).put(patientId, updated);
        return updated;
    }

    @Override
    public void deletePatient(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        patientsByCaregiver(caregiverId).remove(patientId);
        loopRules.values().removeIf(rule -> rule.patientId().equals(patientId));
        dangerousTopics.values().removeIf(topic -> topic.patientId().equals(patientId));
        safeMemories.values().removeIf(memory -> memory.patientId().equals(patientId));
    }

    @Override
    public PairingCodeDto createPairingCode(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        OffsetDateTime now = OffsetDateTime.now();
        String code = generatePairingCode();
        PairingCodeDto pairingCode = new PairingCodeDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                code,
                now.plusMinutes(15),
                null,
                now);
        pairingCodes.put(pairingCode.id(), pairingCode);
        pairingCodesByCode.put(code, pairingCode);
        return pairingCode;
    }

    @Override
    public PatientDeviceDto linkDevice(String code, String deviceIdentifier, String deviceName) {
        PairingCodeDto pairingCode = pairingCodesByCode.get(code.toUpperCase());
        if (pairingCode == null) {
            throw new NotFoundException("Pairing code not found");
        }
        if (pairingCode.consumedAt() != null || pairingCode.expiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Pairing code is no longer valid");
        }

        PairingCodeDto consumed = new PairingCodeDto(
                pairingCode.id(),
                pairingCode.caregiverId(),
                pairingCode.patientId(),
                pairingCode.code(),
                pairingCode.expiresAt(),
                OffsetDateTime.now(),
                pairingCode.createdAt());
        pairingCodes.put(consumed.id(), consumed);
        pairingCodesByCode.put(consumed.code(), consumed);

        PatientDeviceDto device = new PatientDeviceDto(
                UUID.randomUUID(),
                consumed.caregiverId(),
                consumed.patientId(),
                deviceIdentifier,
                deviceName,
                OffsetDateTime.now(),
                null);
        patientDevices.put(device.id(), device);
        return device;
    }

    @Override
    public PatientDeviceDto getActiveDeviceByIdentifier(String deviceIdentifier) {
        return patientDevices.values().stream()
                .filter(device -> device.deviceIdentifier().equals(deviceIdentifier))
                .filter(device -> device.revokedAt() == null)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Patient device not linked"));
    }

    @Override
    public List<PatientDeviceDto> listPatientDevices(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return patientDevices.values().stream()
                .filter(device -> device.caregiverId().equals(caregiverId))
                .filter(device -> device.patientId().equals(patientId))
                .filter(device -> device.revokedAt() == null)
                .sorted(Comparator.comparing(PatientDeviceDto::linkedAt).reversed())
                .toList();
    }

    @Override
    public void revokePatientDevice(String caregiverId, UUID patientId, UUID deviceId) {
        PatientDeviceDto current = listPatientDevices(caregiverId, patientId).stream()
                .filter(device -> device.id().equals(deviceId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Patient device not found"));
        PatientDeviceDto revoked = new PatientDeviceDto(
                current.id(),
                current.caregiverId(),
                current.patientId(),
                current.deviceIdentifier(),
                current.deviceName(),
                current.linkedAt(),
                OffsetDateTime.now());
        patientDevices.put(deviceId, revoked);
    }

    @Override
    public List<LoopRuleDto> listLoopRules(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return loopRules.values().stream()
                .filter(rule -> rule.caregiverId().equals(caregiverId) && rule.patientId().equals(patientId))
                .sorted(Comparator.comparing(LoopRuleDto::createdAt).reversed())
                .toList();
    }

    @Override
    public LoopRuleDto createLoopRule(String caregiverId, UUID patientId, UpsertLoopRuleRequest request) {
        getPatient(caregiverId, patientId);
        OffsetDateTime now = OffsetDateTime.now();
        LoopRuleDto rule = new LoopRuleDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                request.question(),
                request.answer(),
                defaultActive(request.active()),
                now,
                now);
        loopRules.put(rule.id(), rule);
        return rule;
    }

    @Override
    public LoopRuleDto updateLoopRule(String caregiverId, UUID patientId, UUID ruleId, UpsertLoopRuleRequest request) {
        LoopRuleDto current = requireLoopRule(caregiverId, patientId, ruleId);
        LoopRuleDto updated = new LoopRuleDto(
                current.id(),
                current.caregiverId(),
                current.patientId(),
                request.question(),
                request.answer(),
                defaultActive(request.active()),
                current.createdAt(),
                OffsetDateTime.now());
        loopRules.put(ruleId, updated);
        return updated;
    }

    @Override
    public void deleteLoopRule(String caregiverId, UUID patientId, UUID ruleId) {
        requireLoopRule(caregiverId, patientId, ruleId);
        loopRules.remove(ruleId);
    }

    @Override
    public List<DangerousTopicDto> listDangerousTopics(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return dangerousTopics.values().stream()
                .filter(topic -> topic.caregiverId().equals(caregiverId) && topic.patientId().equals(patientId))
                .sorted(Comparator.comparing(DangerousTopicDto::createdAt).reversed())
                .toList();
    }

    @Override
    public DangerousTopicDto createDangerousTopic(String caregiverId, UUID patientId,
            UpsertDangerousTopicRequest request) {
        getPatient(caregiverId, patientId);
        OffsetDateTime now = OffsetDateTime.now();
        DangerousTopicDto topic = new DangerousTopicDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                request.term(),
                request.redirectHint(),
                defaultActive(request.active()),
                now,
                now);
        dangerousTopics.put(topic.id(), topic);
        return topic;
    }

    @Override
    public DangerousTopicDto updateDangerousTopic(String caregiverId, UUID patientId, UUID topicId,
            UpsertDangerousTopicRequest request) {
        DangerousTopicDto current = requireDangerousTopic(caregiverId, patientId, topicId);
        DangerousTopicDto updated = new DangerousTopicDto(
                current.id(),
                current.caregiverId(),
                current.patientId(),
                request.term(),
                request.redirectHint(),
                defaultActive(request.active()),
                current.createdAt(),
                OffsetDateTime.now());
        dangerousTopics.put(topicId, updated);
        return updated;
    }

    @Override
    public void deleteDangerousTopic(String caregiverId, UUID patientId, UUID topicId) {
        requireDangerousTopic(caregiverId, patientId, topicId);
        dangerousTopics.remove(topicId);
    }

    @Override
    public List<SafeMemoryDto> listSafeMemories(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return safeMemories.values().stream()
                .filter(memory -> memory.caregiverId().equals(caregiverId) && memory.patientId().equals(patientId))
                .sorted(Comparator.comparing(SafeMemoryDto::createdAt).reversed())
                .toList();
    }

    @Override
    public SafeMemoryDto createSafeMemory(String caregiverId, UUID patientId, UpsertSafeMemoryRequest request) {
        getPatient(caregiverId, patientId);
        OffsetDateTime now = OffsetDateTime.now();
        SafeMemoryDto memory = new SafeMemoryDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                request.title(),
                request.content(),
                defaultActive(request.active()),
                now,
                now);
        safeMemories.put(memory.id(), memory);
        return memory;
    }

    @Override
    public SafeMemoryDto updateSafeMemory(String caregiverId, UUID patientId, UUID memoryId,
            UpsertSafeMemoryRequest request) {
        SafeMemoryDto current = requireSafeMemory(caregiverId, patientId, memoryId);
        SafeMemoryDto updated = new SafeMemoryDto(
                current.id(),
                current.caregiverId(),
                current.patientId(),
                request.title(),
                request.content(),
                defaultActive(request.active()),
                current.createdAt(),
                OffsetDateTime.now());
        safeMemories.put(memoryId, updated);
        return updated;
    }

    @Override
    public void deleteSafeMemory(String caregiverId, UUID patientId, UUID memoryId) {
        requireSafeMemory(caregiverId, patientId, memoryId);
        safeMemories.remove(memoryId);
    }

    @Override
    public ConversationSessionDto createSession(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        OffsetDateTime now = OffsetDateTime.now();
        ConversationSessionDto session = new ConversationSessionDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                "waiting",
                null,
                null,
                now,
                now,
                now);
        sessions.put(session.id(), session);
        addEvent(caregiverId, patientId, session.id(), "session_created", "Sesion creada", Map.of());
        return session;
    }

    @Override
    public List<ConversationSessionDto> listSessions(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return sessions.values().stream()
                .filter(session -> session.caregiverId().equals(caregiverId) && session.patientId().equals(patientId))
                .sorted(Comparator.comparing(ConversationSessionDto::createdAt).reversed())
                .toList();
    }

    @Override
    public ConversationSessionDto findLatestOpenSession(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return sessions.values().stream()
                .filter(session -> session.caregiverId().equals(caregiverId) && session.patientId().equals(patientId))
                .filter(session -> !"ended".equals(session.status()))
                .sorted(Comparator.comparing(ConversationSessionDto::createdAt).reversed())
                .findFirst()
                .orElse(null);
    }

    @Override
    public ConversationSessionDto getSession(String caregiverId, UUID sessionId) {
        ConversationSessionDto session = sessions.get(sessionId);
        if (session == null || !session.caregiverId().equals(caregiverId)) {
            throw new NotFoundException("Session not found");
        }
        return session;
    }

    @Override
    public ConversationSessionDto updateSessionStatus(String caregiverId, UUID sessionId, String status) {
        ConversationSessionDto current = getSession(caregiverId, sessionId);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startedAt = current.startedAt();
        OffsetDateTime endedAt = current.endedAt();

        if ("active".equals(status) && startedAt == null) {
            startedAt = now;
        }
        if ("ended".equals(status)) {
            endedAt = now;
        }

        ConversationSessionDto updated = new ConversationSessionDto(
                current.id(),
                current.caregiverId(),
                current.patientId(),
                status,
                startedAt,
                endedAt,
                now,
                current.createdAt(),
                now);
        sessions.put(sessionId, updated);
        addEvent(caregiverId, current.patientId(), sessionId, "session_" + status, "Sesion en estado " + status,
                Map.of("status", status));
        return updated;
    }

    @Override
    public ConversationMessageDto addMessage(String caregiverId, UUID patientId, UUID sessionId, String sender,
            String content) {
        ConversationMessageDto message = new ConversationMessageDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                sessionId,
                sender,
                content,
                OffsetDateTime.now());
        messages.put(message.id(), message);
        return message;
    }

    @Override
    public List<ConversationMessageDto> listMessages(String caregiverId, UUID sessionId) {
        ConversationSessionDto session = getSession(caregiverId, sessionId);
        return messages.values().stream()
                .filter(message -> message.sessionId().equals(session.id()))
                .sorted(Comparator.comparing(ConversationMessageDto::createdAt))
                .toList();
    }

    @Override
    public SessionEventDto addEvent(String caregiverId, UUID patientId, UUID sessionId, String eventType,
            String description, Map<String, Object> metadata) {
        SessionEventDto event = new SessionEventDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                sessionId,
                eventType,
                description,
                metadata,
                OffsetDateTime.now());
        events.put(event.id(), event);
        return event;
    }

    @Override
    public List<SessionEventDto> listEvents(String caregiverId, UUID sessionId) {
        ConversationSessionDto session = getSession(caregiverId, sessionId);
        return events.values().stream()
                .filter(event -> event.sessionId().equals(session.id()))
                .sorted(Comparator.comparing(SessionEventDto::createdAt))
                .toList();
    }

    @Override
    public AlertDto addAlert(String caregiverId, UUID patientId, UUID sessionId, String alertType, String severity,
            String title, String message) {
        AlertDto alert = new AlertDto(
                UUID.randomUUID(),
                caregiverId,
                patientId,
                sessionId,
                alertType,
                severity,
                title,
                message,
                null,
                OffsetDateTime.now());
        alerts.put(alert.id(), alert);
        return alert;
    }

    @Override
    public List<AlertDto> listAlerts(String caregiverId) {
        return alerts.values().stream()
                .filter(alert -> alert.caregiverId().equals(caregiverId))
                .sorted(Comparator.comparing(AlertDto::createdAt).reversed())
                .toList();
    }

    @Override
    public AlertDto markAlertRead(String caregiverId, UUID alertId) {
        AlertDto current = alerts.get(alertId);
        if (current == null || !current.caregiverId().equals(caregiverId)) {
            throw new NotFoundException("Alert not found");
        }
        AlertDto updated = new AlertDto(
                current.id(),
                current.caregiverId(),
                current.patientId(),
                current.sessionId(),
                current.alertType(),
                current.severity(),
                current.title(),
                current.message(),
                OffsetDateTime.now(),
                current.createdAt());
        alerts.put(alertId, updated);
        return updated;
    }

    private Map<UUID, PatientDto> patientsByCaregiver(String caregiverId) {
        return patients.computeIfAbsent(caregiverId, ignored -> new ConcurrentHashMap<>());
    }

    private LoopRuleDto requireLoopRule(String caregiverId, UUID patientId, UUID ruleId) {
        LoopRuleDto rule = loopRules.get(ruleId);
        if (rule == null || !rule.caregiverId().equals(caregiverId) || !rule.patientId().equals(patientId)) {
            throw new NotFoundException("Loop rule not found");
        }
        return rule;
    }

    private DangerousTopicDto requireDangerousTopic(String caregiverId, UUID patientId, UUID topicId) {
        DangerousTopicDto topic = dangerousTopics.get(topicId);
        if (topic == null || !topic.caregiverId().equals(caregiverId) || !topic.patientId().equals(patientId)) {
            throw new NotFoundException("Dangerous topic not found");
        }
        return topic;
    }

    private SafeMemoryDto requireSafeMemory(String caregiverId, UUID patientId, UUID memoryId) {
        SafeMemoryDto memory = safeMemories.get(memoryId);
        if (memory == null || !memory.caregiverId().equals(caregiverId) || !memory.patientId().equals(patientId)) {
            throw new NotFoundException("Safe memory not found");
        }
        return memory;
    }

    private String generatePairingCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                builder.append(PAIRING_ALPHABET[RANDOM.nextInt(PAIRING_ALPHABET.length)]);
            }
            code = builder.toString();
        } while (pairingCodesByCode.containsKey(code));
        return code;
    }

    private boolean defaultActive(Boolean active) {
        return active == null || active;
    }

    private String defaultTextSize(String textSize) {
        return textSize == null || textSize.isBlank() ? "normal" : textSize;
    }

    private Double defaultTtsSpeed(Double ttsSpeed) {
        return ttsSpeed == null ? 1.0 : ttsSpeed;
    }

    private String defaultFullName(String fullName) {
        return fullName == null || fullName.isBlank() ? "Cuidador" : fullName.trim();
    }
}
