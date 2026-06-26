package com.sme.erp.audit.dto;

public record AuditRetentionDTO(
        int activityLogRetentionYears,
        int securityLogRetentionYears,
        boolean autoDeleteEnabled,
        String archivePolicy) {
}
