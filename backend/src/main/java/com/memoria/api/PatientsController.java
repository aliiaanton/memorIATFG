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

import com.memoria.api.dto.PatientDto;
import com.memoria.api.dto.UpsertPatientRequest;
import com.memoria.security.CurrentUserProvider;
import com.memoria.service.MemoriaStore;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientsController {

    private final CurrentUserProvider currentUserProvider;
    private final MemoriaStore store;

    public PatientsController(CurrentUserProvider currentUserProvider, MemoriaStore store) {
        this.currentUserProvider = currentUserProvider;
        this.store = store;
    }

    @GetMapping
    public List<PatientDto> list() {
        return store.listPatients(currentUserProvider.caregiverId());
    }

    @PostMapping
    public ResponseEntity<PatientDto> create(@Valid @RequestBody UpsertPatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.createPatient(currentUserProvider.caregiverId(), request));
    }

    @GetMapping("/{patientId}")
    public PatientDto get(@PathVariable UUID patientId) {
        return store.getPatient(currentUserProvider.caregiverId(), patientId);
    }

    @PutMapping("/{patientId}")
    public PatientDto update(@PathVariable UUID patientId, @Valid @RequestBody UpsertPatientRequest request) {
        return store.updatePatient(currentUserProvider.caregiverId(), patientId, request);
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> delete(@PathVariable UUID patientId) {
        store.deletePatient(currentUserProvider.caregiverId(), patientId);
        return ResponseEntity.noContent().build();
    }
}
