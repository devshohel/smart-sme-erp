package com.sme.erp.audit.dto;

import java.time.LocalDateTime;

public record ActivityLogDTO(
        Long id,
        Long userId,
        String username,
        String action,
        String module,
        String tableName,
        Long recordId,
        Long entityId,
        String ipAddress,
        String userAgent,
        String details,
        String oldValue,
        String newValue,
        LocalDateTime createdAt) {
}
