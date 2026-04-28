package com.memoria.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memoria.api.dto.ConversationMessageDto;
import com.memoria.api.dto.ConversationResponseDto;
import com.memoria.api.dto.ConversationSessionDto;
import com.memoria.api.dto.PatientMessageRequest;
import com.memoria.api.dto.SessionEventDto;
import com.memoria.security.CurrentUserProvider;
import com.memoria.service.ConversationService;
import com.memoria.service.MemoriaStore;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SessionsController {

    private final CurrentUserProvider currentUserProvider;
    private final MemoriaStore store;
    private final ConversationService conversationService;

    public SessionsController(CurrentUserProvider currentUserProvider, MemoriaStore store,
            ConversationService conversationService) {
        this.currentUserProvider = currentUserProvider;
        this.store = store;
        this.conversationService = conversationService;
    }

    @PostMapping("/patients/{patientId}/sessions")
    public ResponseEntity<ConversationSessionDto> createSession(@PathVariable UUID patientId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.createSession(currentUserProvider.caregiverId(), patientId));
    }

    @GetMapping("/patients/{patientId}/sessions")
    public List<ConversationSessionDto> listSessions(@PathVariable UUID patientId) {
        return store.listSessions(currentUserProvider.caregiverId(), patientId);
    }

    @GetMapping("/sessions/{sessionId}")
    public ConversationSessionDto getSession(@PathVariable UUID sessionId) {
        return store.getSession(currentUserProvider.caregiverId(), sessionId);
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ConversationSessionDto start(@PathVariable UUID sessionId) {
        return store.updateSessionStatus(currentUserProvider.caregiverId(), sessionId, "active");
    }

    @PostMapping("/sessions/{sessionId}/pause")
    public ConversationSessionDto pause(@PathVariable UUID sessionId) {
        return store.updateSessionStatus(currentUserProvider.caregiverId(), sessionId, "paused");
    }

    @PostMapping("/sessions/{sessionId}/resume")
    public ConversationSessionDto resume(@PathVariable UUID sessionId) {
        return store.updateSessionStatus(currentUserProvider.caregiverId(), sessionId, "active");
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ConversationSessionDto end(@PathVariable UUID sessionId) {
        return store.updateSessionStatus(currentUserProvider.caregiverId(), sessionId, "ended");
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ConversationResponseDto handleMessage(@PathVariable UUID sessionId,
            @Valid @RequestBody PatientMessageRequest request) {
        return conversationService.handlePatientMessage(currentUserProvider.caregiverId(), sessionId, request);
    }

    @GetMapping("/sessions/{sessionId}/transcript")
    public List<ConversationMessageDto> transcript(@PathVariable UUID sessionId) {
        return store.listMessages(currentUserProvider.caregiverId(), sessionId);
    }

    @GetMapping("/sessions/{sessionId}/events")
    public List<SessionEventDto> events(@PathVariable UUID sessionId) {
        return store.listEvents(currentUserProvider.caregiverId(), sessionId);
    }
}
