package com.memoria.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memoria.api.dto.DangerousTopicDto;
import com.memoria.api.dto.LoopRuleDto;
import com.memoria.api.dto.SafeMemoryDto;
import com.memoria.api.dto.UpsertDangerousTopicRequest;
import com.memoria.api.dto.UpsertLoopRuleRequest;
import com.memoria.api.dto.UpsertSafeMemoryRequest;
import com.memoria.security.CurrentUserProvider;
import com.memoria.service.MemoriaStore;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients/{patientId}")
public class AiConfigController {

    private final CurrentUserProvider currentUserProvider;
    private final MemoriaStore store;

    public AiConfigController(CurrentUserProvider currentUserProvider, MemoriaStore store) {
        this.currentUserProvider = currentUserProvider;
        this.store = store;
    }

    @GetMapping("/loop-rules")
    public List<LoopRuleDto> listLoopRules(@PathVariable UUID patientId) {
        return store.listLoopRules(currentUserProvider.caregiverId(), patientId);
    }

    @PostMapping("/loop-rules")
    public ResponseEntity<LoopRuleDto> createLoopRule(@PathVariable UUID patientId,
            @Valid @RequestBody UpsertLoopRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.createLoopRule(currentUserProvider.caregiverId(), patientId, request));
    }

    @PutMapping("/loop-rules/{ruleId}")
    public LoopRuleDto updateLoopRule(@PathVariable UUID patientId, @PathVariable UUID ruleId,
            @Valid @RequestBody UpsertLoopRuleRequest request) {
        return store.updateLoopRule(currentUserProvider.caregiverId(), patientId, ruleId, request);
    }

    @DeleteMapping("/loop-rules/{ruleId}")
    public ResponseEntity<Void> deleteLoopRule(@PathVariable UUID patientId, @PathVariable UUID ruleId) {
        store.deleteLoopRule(currentUserProvider.caregiverId(), patientId, ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dangerous-topics")
    public List<DangerousTopicDto> listDangerousTopics(@PathVariable UUID patientId) {
        return store.listDangerousTopics(currentUserProvider.caregiverId(), patientId);
    }

    @PostMapping("/dangerous-topics")
    public ResponseEntity<DangerousTopicDto> createDangerousTopic(@PathVariable UUID patientId,
            @Valid @RequestBody UpsertDangerousTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.createDangerousTopic(currentUserProvider.caregiverId(), patientId, request));
    }

    @PutMapping("/dangerous-topics/{topicId}")
    public DangerousTopicDto updateDangerousTopic(@PathVariable UUID patientId, @PathVariable UUID topicId,
            @Valid @RequestBody UpsertDangerousTopicRequest request) {
        return store.updateDangerousTopic(currentUserProvider.caregiverId(), patientId, topicId, request);
    }

    @DeleteMapping("/dangerous-topics/{topicId}")
    public ResponseEntity<Void> deleteDangerousTopic(@PathVariable UUID patientId, @PathVariable UUID topicId) {
        store.deleteDangerousTopic(currentUserProvider.caregiverId(), patientId, topicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/safe-memories")
    public List<SafeMemoryDto> listSafeMemories(@PathVariable UUID patientId) {
        return store.listSafeMemories(currentUserProvider.caregiverId(), patientId);
    }

    @PostMapping("/safe-memories")
    public ResponseEntity<SafeMemoryDto> createSafeMemory(@PathVariable UUID patientId,
            @Valid @RequestBody UpsertSafeMemoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.createSafeMemory(currentUserProvider.caregiverId(), patientId, request));
    }

    @PutMapping("/safe-memories/{memoryId}")
    public SafeMemoryDto updateSafeMemory(@PathVariable UUID patientId, @PathVariable UUID memoryId,
            @Valid @RequestBody UpsertSafeMemoryRequest request) {
        return store.updateSafeMemory(currentUserProvider.caregiverId(), patientId, memoryId, request);
    }

    @DeleteMapping("/safe-memories/{memoryId}")
    public ResponseEntity<Void> deleteSafeMemory(@PathVariable UUID patientId, @PathVariable UUID memoryId) {
        store.deleteSafeMemory(currentUserProvider.caregiverId(), patientId, memoryId);
        return ResponseEntity.noContent().build();
    }
}
