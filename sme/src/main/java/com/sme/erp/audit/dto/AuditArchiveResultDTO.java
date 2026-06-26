package com.sme.erp.audit.dto;

public record AuditArchiveResultDTO(
        int activityLogsArchived,
        int auditLogsArchived,
        int securityLogsArchived) {
}
