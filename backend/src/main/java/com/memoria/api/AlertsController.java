package com.memoria.api;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memoria.api.dto.AlertDto;
import com.memoria.security.CurrentUserProvider;
import com.memoria.service.MemoriaStore;

@RestController
@RequestMapping("/api/alerts")
public class AlertsController {

    private final CurrentUserProvider currentUserProvider;
    private final MemoriaStore store;

    public AlertsController(CurrentUserProvider currentUserProvider, MemoriaStore store) {
        this.currentUserProvider = currentUserProvider;
        this.store = store;
    }

    @GetMapping
    public List<AlertDto> listAlerts() {
        return store.listAlerts(currentUserProvider.caregiverId());
    }

    @PostMapping("/{alertId}/read")
    public AlertDto markRead(@PathVariable UUID alertId) {
        return store.markAlertRead(currentUserProvider.caregiverId(), alertId);
    }
}
