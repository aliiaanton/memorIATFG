package com.memoria.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    @Value("${app.demo-caregiver-id:00000000-0000-0000-0000-000000000001}")
    private String demoCaregiverId;

    public String caregiverId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return demoCaregiverId;
        }

        Object principal = authentication.getPrincipal();
        if ("anonymousUser".equals(principal)) {
            return demoCaregiverId;
        }

        return authentication.getName();
    }
}
