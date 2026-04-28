package com.memoria.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memoria.api.dto.LinkDeviceRequest;
import com.memoria.api.dto.PairingCodeDto;
import com.memoria.api.dto.PatientDeviceDto;
import com.memoria.security.CurrentUserProvider;
import com.memoria.service.MemoriaStore;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PairingController {

    private final CurrentUserProvider currentUserProvider;
    private final MemoriaStore store;

    public PairingController(CurrentUserProvider currentUserProvider, MemoriaStore store) {
        this.currentUserProvider = currentUserProvider;
        this.store = store;
    }

    @PostMapping("/patients/{patientId}/pairing-codes")
    public ResponseEntity<PairingCodeDto> createPairingCode(@PathVariable UUID patientId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.createPairingCode(currentUserProvider.caregiverId(), patientId));
    }

    @PostMapping("/patient-devices/link")
    public ResponseEntity<PatientDeviceDto> linkDevice(@Valid @RequestBody LinkDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(store.linkDevice(request.code(), request.deviceIdentifier(), request.deviceName()));
    }
}
