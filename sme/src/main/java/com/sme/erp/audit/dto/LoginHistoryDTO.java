package com.sme.erp.audit.dto;

import java.time.LocalDateTime;

public record LoginHistoryDTO(
        Long id,
        Long userId,
        String username,
        String status,
        String ipAddress,
        String userAgent,
        String failureReason,
        LocalDateTime createdAt) {
}
