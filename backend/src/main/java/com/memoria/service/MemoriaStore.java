package com.memoria.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.memoria.api.dto.UpsertDangerousTopicRequest;
import com.memoria.api.dto.UpsertCaregiverProfileRequest;
import com.memoria.api.dto.UpsertLoopRuleRequest;
import com.memoria.api.dto.UpsertPatientRequest;
import com.memoria.api.dto.UpsertSafeMemoryRequest;

public interface MemoriaStore {

    CaregiverProfileDto getOrCreateCaregiverProfile(String caregiverId, String fallbackFullName);

    CaregiverProfileDto updateCaregiverProfile(String caregiverId, UpsertCaregiverProfileRequest request);

    List<PatientDto> listPatients(String caregiverId);

    PatientDto createPatient(String caregiverId, UpsertPatientRequest request);

    PatientDto getPatient(String caregiverId, UUID patientId);

    PatientDto updatePatient(String caregiverId, UUID patientId, UpsertPatientRequest request);

    void deletePatient(String caregiverId, UUID patientId);

    PairingCodeDto createPairingCode(String caregiverId, UUID patientId);

    PatientDeviceDto linkDevice(String code, String deviceIdentifier, String deviceName);

    PatientDeviceDto getActiveDeviceByIdentifier(String deviceIdentifier);

    List<PatientDeviceDto> listPatientDevices(String caregiverId, UUID patientId);

    void revokePatientDevice(String caregiverId, UUID patientId, UUID deviceId);

    List<LoopRuleDto> listLoopRules(String caregiverId, UUID patientId);

    LoopRuleDto createLoopRule(String caregiverId, UUID patientId, UpsertLoopRuleRequest request);

    LoopRuleDto updateLoopRule(String caregiverId, UUID patientId, UUID ruleId, UpsertLoopRuleRequest request);

    void deleteLoopRule(String caregiverId, UUID patientId, UUID ruleId);

    List<DangerousTopicDto> listDangerousTopics(String caregiverId, UUID patientId);

    DangerousTopicDto createDangerousTopic(String caregiverId, UUID patientId, UpsertDangerousTopicRequest request);

    DangerousTopicDto updateDangerousTopic(String caregiverId, UUID patientId, UUID topicId,
            UpsertDangerousTopicRequest request);

    void deleteDangerousTopic(String caregiverId, UUID patientId, UUID topicId);

    List<SafeMemoryDto> listSafeMemories(String caregiverId, UUID patientId);

    SafeMemoryDto createSafeMemory(String caregiverId, UUID patientId, UpsertSafeMemoryRequest request);

    SafeMemoryDto updateSafeMemory(String caregiverId, UUID patientId, UUID memoryId, UpsertSafeMemoryRequest request);

    void deleteSafeMemory(String caregiverId, UUID patientId, UUID memoryId);

    ConversationSessionDto createSession(String caregiverId, UUID patientId);

    List<ConversationSessionDto> listSessions(String caregiverId, UUID patientId);

    ConversationSessionDto findLatestOpenSession(String caregiverId, UUID patientId);

    ConversationSessionDto getSession(String caregiverId, UUID sessionId);

    ConversationSessionDto updateSessionStatus(String caregiverId, UUID sessionId, String status);

    ConversationMessageDto addMessage(String caregiverId, UUID patientId, UUID sessionId, String sender, String content);

    List<ConversationMessageDto> listMessages(String caregiverId, UUID sessionId);

    SessionEventDto addEvent(String caregiverId, UUID patientId, UUID sessionId, String eventType,
            String description, Map<String, Object> metadata);

    List<SessionEventDto> listEvents(String caregiverId, UUID sessionId);

    AlertDto addAlert(String caregiverId, UUID patientId, UUID sessionId, String alertType, String severity,
            String title, String message);

    List<AlertDto> listAlerts(String caregiverId);

    AlertDto markAlertRead(String caregiverId, UUID alertId);
}
