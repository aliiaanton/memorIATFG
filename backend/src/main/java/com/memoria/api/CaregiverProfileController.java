package com.memoria.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memoria.api.dto.CaregiverProfileDto;
import com.memoria.api.dto.UpsertCaregiverProfileRequest;
import com.memoria.security.CurrentUserProvider;
import com.memoria.service.MemoriaStore;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/me/profile")
public class CaregiverProfileController {

    private final CurrentUserProvider currentUserProvider;
    private final MemoriaStore store;

    public CaregiverProfileController(CurrentUserProvider currentUserProvider, MemoriaStore store) {
        this.currentUserProvider = currentUserProvider;
        this.store = store;
    }

    @GetMapping
    public CaregiverProfileDto get() {
        return store.getOrCreateCaregiverProfile(currentUserProvider.caregiverId(), "Cuidador");
    }

    @PutMapping
    public CaregiverProfileDto update(@Valid @RequestBody UpsertCaregiverProfileRequest request) {
        return store.updateCaregiverProfile(currentUserProvider.caregiverId(), request);
    }
}
