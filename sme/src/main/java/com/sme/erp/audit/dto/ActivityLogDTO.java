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
        String ipAddress,
        String details,
        LocalDateTime createdAt) {
}
