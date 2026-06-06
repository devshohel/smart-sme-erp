package com.sme.erp.audit.dto;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        Long userId,
        String username,
        String tableName,
        Long recordId,
        String oldData,
        String newData,
        String action,
        LocalDateTime createdAt) {
}
