package com.memoria.service;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.memoria.api.dto.AlertDto;
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
import com.memoria.api.dto.UpsertLoopRuleRequest;
import com.memoria.api.dto.UpsertPatientRequest;
import com.memoria.api.dto.UpsertSafeMemoryRequest;

@Service
@ConditionalOnProperty(name = "app.store", havingValue = "supabase")
public class SupabaseJdbcStore implements MemoriaStore {

    private static final char[] PAIRING_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;

    public SupabaseJdbcStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<PatientDto> listPatients(String caregiverId) {
        ensureCaregiver(caregiverId);
        return jdbc.query("""
                select * from public.patients
                where caregiver_id = ?
                order by created_at desc
                """, patientMapper(), uuid(caregiverId));
    }

    @Override
    public PatientDto createPatient(String caregiverId, UpsertPatientRequest request) {
        ensureCaregiver(caregiverId);
        return jdbc.queryForObject("""
                insert into public.patients
                (caregiver_id, full_name, preferred_name, birth_year, relationship, notes, text_size, tts_speed)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                returning *
                """, patientMapper(),
                uuid(caregiverId),
                request.fullName(),
                request.preferredName(),
                request.birthYear(),
                request.relationship(),
                request.notes(),
                defaultTextSize(request.textSize()),
                defaultTtsSpeed(request.ttsSpeed()));
    }

    @Override
    public PatientDto getPatient(String caregiverId, UUID patientId) {
        ensureCaregiver(caregiverId);
        try {
            return jdbc.queryForObject("""
                    select * from public.patients
                    where caregiver_id = ? and id = ?
                    """, patientMapper(), uuid(caregiverId), patientId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Patient not found");
        }
    }

    @Override
    public PatientDto updatePatient(String caregiverId, UUID patientId, UpsertPatientRequest request) {
        getPatient(caregiverId, patientId);
        return jdbc.queryForObject("""
                update public.patients
                set full_name = ?, preferred_name = ?, birth_year = ?, relationship = ?, notes = ?,
                    text_size = ?, tts_speed = ?
                where caregiver_id = ? and id = ?
                returning *
                """, patientMapper(),
                request.fullName(),
                request.preferredName(),
                request.birthYear(),
                request.relationship(),
                request.notes(),
                defaultTextSize(request.textSize()),
                defaultTtsSpeed(request.ttsSpeed()),
                uuid(caregiverId),
                patientId);
    }

    @Override
    public void deletePatient(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        jdbc.update("delete from public.patients where caregiver_id = ? and id = ?", uuid(caregiverId), patientId);
    }

    @Override
    public PairingCodeDto createPairingCode(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        String code = generatePairingCode();
        return jdbc.queryForObject("""
                insert into public.pairing_codes (caregiver_id, patient_id, code, expires_at)
                values (?, ?, ?, now() + interval '15 minutes')
                returning *
                """, pairingMapper(), uuid(caregiverId), patientId, code);
    }

    @Override
    public PatientDeviceDto linkDevice(String code, String deviceIdentifier, String deviceName) {
        PairingCodeDto pairingCode = findPairingCode(code);
        if (pairingCode.consumedAt() != null || pairingCode.expiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Pairing code is no longer valid");
        }
        jdbc.update("update public.pairing_codes set consumed_at = now() where id = ?", pairingCode.id());
        return jdbc.queryForObject("""
                insert into public.patient_devices (caregiver_id, patient_id, device_identifier, device_name)
                values (?, ?, ?, ?)
                on conflict (patient_id, device_identifier)
                do update set revoked_at = null, device_name = excluded.device_name, linked_at = now()
                returning *
                """, deviceMapper(), uuid(pairingCode.caregiverId()), pairingCode.patientId(), deviceIdentifier,
                deviceName);
    }

    @Override
    public PatientDeviceDto getActiveDeviceByIdentifier(String deviceIdentifier) {
        try {
            return jdbc.queryForObject("""
                    select * from public.patient_devices
                    where device_identifier = ? and revoked_at is null
                    order by linked_at desc
                    limit 1
                    """, deviceMapper(), deviceIdentifier);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Patient device not linked");
        }
    }

    @Override
    public List<LoopRuleDto> listLoopRules(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return jdbc.query("""
                select * from public.loop_rules
                where caregiver_id = ? and patient_id = ?
                order by created_at desc
                """, loopRuleMapper(), uuid(caregiverId), patientId);
    }

    @Override
    public LoopRuleDto createLoopRule(String caregiverId, UUID patientId, UpsertLoopRuleRequest request) {
        getPatient(caregiverId, patientId);
        return jdbc.queryForObject("""
                insert into public.loop_rules (caregiver_id, patient_id, question, answer, is_active)
                values (?, ?, ?, ?, ?)
                returning *
                """, loopRuleMapper(), uuid(caregiverId), patientId, request.question(), request.answer(),
                defaultActive(request.active()));
    }

    @Override
    public LoopRuleDto updateLoopRule(String caregiverId, UUID patientId, UUID ruleId, UpsertLoopRuleRequest request) {
        requireLoopRule(caregiverId, patientId, ruleId);
        return jdbc.queryForObject("""
                update public.loop_rules
                set question = ?, answer = ?, is_active = ?
                where caregiver_id = ? and patient_id = ? and id = ?
                returning *
                """, loopRuleMapper(), request.question(), request.answer(), defaultActive(request.active()),
                uuid(caregiverId), patientId, ruleId);
    }

    @Override
    public void deleteLoopRule(String caregiverId, UUID patientId, UUID ruleId) {
        requireLoopRule(caregiverId, patientId, ruleId);
        jdbc.update("delete from public.loop_rules where caregiver_id = ? and patient_id = ? and id = ?",
                uuid(caregiverId), patientId, ruleId);
    }

    @Override
    public List<DangerousTopicDto> listDangerousTopics(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return jdbc.query("""
                select * from public.dangerous_topics
                where caregiver_id = ? and patient_id = ?
                order by created_at desc
                """, dangerousTopicMapper(), uuid(caregiverId), patientId);
    }

    @Override
    public DangerousTopicDto createDangerousTopic(String caregiverId, UUID patientId,
            UpsertDangerousTopicRequest request) {
        getPatient(caregiverId, patientId);
        return jdbc.queryForObject("""
                insert into public.dangerous_topics (caregiver_id, patient_id, term, redirect_hint, is_active)
                values (?, ?, ?, ?, ?)
                returning *
                """, dangerousTopicMapper(), uuid(caregiverId), patientId, request.term(), request.redirectHint(),
                defaultActive(request.active()));
    }

    @Override
    public DangerousTopicDto updateDangerousTopic(String caregiverId, UUID patientId, UUID topicId,
            UpsertDangerousTopicRequest request) {
        requireDangerousTopic(caregiverId, patientId, topicId);
        return jdbc.queryForObject("""
                update public.dangerous_topics
                set term = ?, redirect_hint = ?, is_active = ?
                where caregiver_id = ? and patient_id = ? and id = ?
                returning *
                """, dangerousTopicMapper(), request.term(), request.redirectHint(),
                defaultActive(request.active()), uuid(caregiverId), patientId, topicId);
    }

    @Override
    public void deleteDangerousTopic(String caregiverId, UUID patientId, UUID topicId) {
        requireDangerousTopic(caregiverId, patientId, topicId);
        jdbc.update("delete from public.dangerous_topics where caregiver_id = ? and patient_id = ? and id = ?",
                uuid(caregiverId), patientId, topicId);
    }

    @Override
    public List<SafeMemoryDto> listSafeMemories(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return jdbc.query("""
                select * from public.safe_memories
                where caregiver_id = ? and patient_id = ?
                order by created_at desc
                """, safeMemoryMapper(), uuid(caregiverId), patientId);
    }

    @Override
    public SafeMemoryDto createSafeMemory(String caregiverId, UUID patientId, UpsertSafeMemoryRequest request) {
        getPatient(caregiverId, patientId);
        return jdbc.queryForObject("""
                insert into public.safe_memories (caregiver_id, patient_id, title, content, is_active)
                values (?, ?, ?, ?, ?)
                returning *
                """, safeMemoryMapper(), uuid(caregiverId), patientId, request.title(), request.content(),
                defaultActive(request.active()));
    }

    @Override
    public SafeMemoryDto updateSafeMemory(String caregiverId, UUID patientId, UUID memoryId,
            UpsertSafeMemoryRequest request) {
        requireSafeMemory(caregiverId, patientId, memoryId);
        return jdbc.queryForObject("""
                update public.safe_memories
                set title = ?, content = ?, is_active = ?
                where caregiver_id = ? and patient_id = ? and id = ?
                returning *
                """, safeMemoryMapper(), request.title(), request.content(), defaultActive(request.active()),
                uuid(caregiverId), patientId, memoryId);
    }

    @Override
    public void deleteSafeMemory(String caregiverId, UUID patientId, UUID memoryId) {
        requireSafeMemory(caregiverId, patientId, memoryId);
        jdbc.update("delete from public.safe_memories where caregiver_id = ? and patient_id = ? and id = ?",
                uuid(caregiverId), patientId, memoryId);
    }

    @Override
    public ConversationSessionDto createSession(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        ConversationSessionDto session = jdbc.queryForObject("""
                insert into public.conversation_sessions (caregiver_id, patient_id, status, last_event_at)
                values (?, ?, 'waiting', now())
                returning *
                """, sessionMapper(), uuid(caregiverId), patientId);
        addEvent(caregiverId, patientId, session.id(), "session_created", "Sesion creada", Map.of());
        return session;
    }

    @Override
    public List<ConversationSessionDto> listSessions(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        return jdbc.query("""
                select * from public.conversation_sessions
                where caregiver_id = ? and patient_id = ?
                order by created_at desc
                """, sessionMapper(), uuid(caregiverId), patientId);
    }

    @Override
    public ConversationSessionDto findLatestOpenSession(String caregiverId, UUID patientId) {
        getPatient(caregiverId, patientId);
        try {
            return jdbc.queryForObject("""
                    select * from public.conversation_sessions
                    where caregiver_id = ? and patient_id = ? and status <> 'ended'
                    order by created_at desc
                    limit 1
                    """, sessionMapper(), uuid(caregiverId), patientId);
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }

    @Override
    public ConversationSessionDto getSession(String caregiverId, UUID sessionId) {
        try {
            return jdbc.queryForObject("""
                    select * from public.conversation_sessions
                    where caregiver_id = ? and id = ?
                    """, sessionMapper(), uuid(caregiverId), sessionId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Session not found");
        }
    }

    @Override
    public ConversationSessionDto updateSessionStatus(String caregiverId, UUID sessionId, String status) {
        ConversationSessionDto current = getSession(caregiverId, sessionId);
        ConversationSessionDto updated = jdbc.queryForObject("""
                update public.conversation_sessions
                set status = ?,
                    started_at = case when ? = 'active' and started_at is null then now() else started_at end,
                    ended_at = case when ? = 'ended' then now() else ended_at end,
                    last_event_at = now()
                where caregiver_id = ? and id = ?
                returning *
                """, sessionMapper(), status, status, status, uuid(caregiverId), sessionId);
        addEvent(caregiverId, current.patientId(), sessionId, "session_" + status, "Sesion en estado " + status,
                Map.of("status", status));
        return updated;
    }

    @Override
    public ConversationMessageDto addMessage(String caregiverId, UUID patientId, UUID sessionId, String sender,
            String content) {
        getSession(caregiverId, sessionId);
        return jdbc.queryForObject("""
                insert into public.conversation_messages (caregiver_id, patient_id, session_id, sender, content)
                values (?, ?, ?, ?, ?)
                returning *
                """, messageMapper(), uuid(caregiverId), patientId, sessionId, sender, content);
    }

    @Override
    public List<ConversationMessageDto> listMessages(String caregiverId, UUID sessionId) {
        getSession(caregiverId, sessionId);
        return jdbc.query("""
                select * from public.conversation_messages
                where caregiver_id = ? and session_id = ?
                order by created_at
                """, messageMapper(), uuid(caregiverId), sessionId);
    }

    @Override
    public SessionEventDto addEvent(String caregiverId, UUID patientId, UUID sessionId, String eventType,
            String description, Map<String, Object> metadata) {
        return jdbc.queryForObject("""
                insert into public.session_events
                (caregiver_id, patient_id, session_id, event_type, description, metadata)
                values (?, ?, ?, ?, ?, '{}'::jsonb)
                returning *
                """, eventMapper(), uuid(caregiverId), patientId, sessionId, eventType, description);
    }

    @Override
    public List<SessionEventDto> listEvents(String caregiverId, UUID sessionId) {
        getSession(caregiverId, sessionId);
        return jdbc.query("""
                select * from public.session_events
                where caregiver_id = ? and session_id = ?
                order by created_at
                """, eventMapper(), uuid(caregiverId), sessionId);
    }

    @Override
    public AlertDto addAlert(String caregiverId, UUID patientId, UUID sessionId, String alertType, String severity,
            String title, String message) {
        return jdbc.queryForObject("""
                insert into public.alerts
                (caregiver_id, patient_id, session_id, alert_type, severity, title, message)
                values (?, ?, ?, ?, ?, ?, ?)
                returning *
                """, alertMapper(), uuid(caregiverId), patientId, sessionId, alertType, severity, title, message);
    }

    @Override
    public List<AlertDto> listAlerts(String caregiverId) {
        ensureCaregiver(caregiverId);
        return jdbc.query("""
                select * from public.alerts
                where caregiver_id = ?
                order by created_at desc
                """, alertMapper(), uuid(caregiverId));
    }

    @Override
    public AlertDto markAlertRead(String caregiverId, UUID alertId) {
        try {
            return jdbc.queryForObject("""
                    update public.alerts
                    set read_at = now()
                    where caregiver_id = ? and id = ?
                    returning *
                    """, alertMapper(), uuid(caregiverId), alertId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Alert not found");
        }
    }

    private void ensureCaregiver(String caregiverId) {
        jdbc.update("""
                insert into public.caregiver_profiles (id, full_name)
                values (?, 'Cuidador demo')
                on conflict (id) do nothing
                """, uuid(caregiverId));
    }

    private PairingCodeDto findPairingCode(String code) {
        try {
            return jdbc.queryForObject("""
                    select * from public.pairing_codes
                    where code = ?
                    """, pairingMapper(), code.toUpperCase());
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Pairing code not found");
        }
    }

    private LoopRuleDto requireLoopRule(String caregiverId, UUID patientId, UUID ruleId) {
        try {
            return jdbc.queryForObject("""
                    select * from public.loop_rules
                    where caregiver_id = ? and patient_id = ? and id = ?
                    """, loopRuleMapper(), uuid(caregiverId), patientId, ruleId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Loop rule not found");
        }
    }

    private DangerousTopicDto requireDangerousTopic(String caregiverId, UUID patientId, UUID topicId) {
        try {
            return jdbc.queryForObject("""
                    select * from public.dangerous_topics
                    where caregiver_id = ? and patient_id = ? and id = ?
                    """, dangerousTopicMapper(), uuid(caregiverId), patientId, topicId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Dangerous topic not found");
        }
    }

    private SafeMemoryDto requireSafeMemory(String caregiverId, UUID patientId, UUID memoryId) {
        try {
            return jdbc.queryForObject("""
                    select * from public.safe_memories
                    where caregiver_id = ? and patient_id = ? and id = ?
                    """, safeMemoryMapper(), uuid(caregiverId), patientId, memoryId);
        } catch (EmptyResultDataAccessException exception) {
            throw new NotFoundException("Safe memory not found");
        }
    }

    private String generatePairingCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(PAIRING_ALPHABET[RANDOM.nextInt(PAIRING_ALPHABET.length)]);
        }
        return builder.toString();
    }

    private UUID uuid(String value) {
        return UUID.fromString(value);
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

    private RowMapper<PatientDto> patientMapper() {
        return (rs, rowNum) -> new PatientDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getString("full_name"),
                rs.getString("preferred_name"),
                (Integer) rs.getObject("birth_year"),
                rs.getString("relationship"),
                rs.getString("notes"),
                rs.getString("text_size"),
                rs.getDouble("tts_speed"),
                offset(rs, "created_at"),
                offset(rs, "updated_at"));
    }

    private RowMapper<PairingCodeDto> pairingMapper() {
        return (rs, rowNum) -> new PairingCodeDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getString("code"),
                offset(rs, "expires_at"),
                offset(rs, "consumed_at"),
                offset(rs, "created_at"));
    }

    private RowMapper<PatientDeviceDto> deviceMapper() {
        return (rs, rowNum) -> new PatientDeviceDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getString("device_identifier"),
                rs.getString("device_name"),
                offset(rs, "linked_at"),
                offset(rs, "revoked_at"));
    }

    private RowMapper<LoopRuleDto> loopRuleMapper() {
        return (rs, rowNum) -> new LoopRuleDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getString("question"),
                rs.getString("answer"),
                rs.getBoolean("is_active"),
                offset(rs, "created_at"),
                offset(rs, "updated_at"));
    }

    private RowMapper<DangerousTopicDto> dangerousTopicMapper() {
        return (rs, rowNum) -> new DangerousTopicDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getString("term"),
                rs.getString("redirect_hint"),
                rs.getBoolean("is_active"),
                offset(rs, "created_at"),
                offset(rs, "updated_at"));
    }

    private RowMapper<SafeMemoryDto> safeMemoryMapper() {
        return (rs, rowNum) -> new SafeMemoryDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getString("title"),
                rs.getString("content"),
                rs.getBoolean("is_active"),
                offset(rs, "created_at"),
                offset(rs, "updated_at"));
    }

    private RowMapper<ConversationSessionDto> sessionMapper() {
        return (rs, rowNum) -> new ConversationSessionDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getString("status"),
                offset(rs, "started_at"),
                offset(rs, "ended_at"),
                offset(rs, "last_event_at"),
                offset(rs, "created_at"),
                offset(rs, "updated_at"));
    }

    private RowMapper<ConversationMessageDto> messageMapper() {
        return (rs, rowNum) -> new ConversationMessageDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getObject("session_id", UUID.class),
                rs.getString("sender"),
                rs.getString("content"),
                offset(rs, "created_at"));
    }

    private RowMapper<SessionEventDto> eventMapper() {
        return (rs, rowNum) -> new SessionEventDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getObject("session_id", UUID.class),
                rs.getString("event_type"),
                rs.getString("description"),
                Map.of(),
                offset(rs, "created_at"));
    }

    private RowMapper<AlertDto> alertMapper() {
        return (rs, rowNum) -> new AlertDto(
                rs.getObject("id", UUID.class),
                rs.getObject("caregiver_id", UUID.class).toString(),
                rs.getObject("patient_id", UUID.class),
                rs.getObject("session_id", UUID.class),
                rs.getString("alert_type"),
                rs.getString("severity"),
                rs.getString("title"),
                rs.getString("message"),
                offset(rs, "read_at"),
                offset(rs, "created_at"));
    }

    private OffsetDateTime offset(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class);
    }
}

