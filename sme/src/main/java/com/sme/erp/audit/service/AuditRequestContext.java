package com.sme.erp.audit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class AuditRequestContext {
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public AuditRequestContext(ObjectProvider<HttpServletRequest> requestProvider) {
        this.requestProvider = requestProvider;
    }

    public String ipAddress() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public String userAgent() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        return request != null ? request.getHeader("User-Agent") : null;
    }
}
