package com.memoria.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memoria.api.dto.ConversationResponseDto;
import com.memoria.api.dto.ConversationSessionDto;
import com.memoria.api.dto.PatientDeviceDto;
import com.memoria.api.dto.PatientDto;
import com.memoria.api.dto.PatientMessageRequest;
import com.memoria.api.dto.PatientTerminalStatusDto;
import com.memoria.service.ConversationService;
import com.memoria.service.MemoriaStore;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient-terminal")
public class PatientTerminalController {

    private final MemoriaStore store;
    private final ConversationService conversationService;

    public PatientTerminalController(MemoriaStore store, ConversationService conversationService) {
        this.store = store;
        this.conversationService = conversationService;
    }

    @GetMapping("/{deviceIdentifier}/status")
    public PatientTerminalStatusDto status(@PathVariable String deviceIdentifier) {
        PatientDeviceDto device = store.getActiveDeviceByIdentifier(deviceIdentifier);
        PatientDto patient = store.getPatient(device.caregiverId(), device.patientId());
        ConversationSessionDto session = store.findLatestOpenSession(device.caregiverId(), device.patientId());

        UUID sessionId = session == null ? null : session.id();
        String sessionStatus = session == null ? "waiting" : session.status();

        return new PatientTerminalStatusDto(true, patient.id(), patient.preferredName(), sessionId, sessionStatus);
    }

    @PostMapping("/{deviceIdentifier}/sessions/{sessionId}/messages")
    public ConversationResponseDto message(@PathVariable String deviceIdentifier, @PathVariable UUID sessionId,
            @Valid @RequestBody PatientMessageRequest request) {
        PatientDeviceDto device = store.getActiveDeviceByIdentifier(deviceIdentifier);
        ConversationSessionDto session = store.getSession(device.caregiverId(), sessionId);

        if (!session.patientId().equals(device.patientId())) {
            throw new IllegalStateException("Session does not belong to linked patient");
        }

        return conversationService.handlePatientMessage(device.caregiverId(), sessionId, request);
    }
}
