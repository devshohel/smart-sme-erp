package com.sme.erp.audit.service.impl;

import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAuditUser {
    private final UserRepository userRepository;

    public CurrentAuditUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
