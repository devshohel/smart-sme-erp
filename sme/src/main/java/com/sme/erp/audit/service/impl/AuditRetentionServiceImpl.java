package com.sme.erp.audit.service.impl;

import com.sme.erp.audit.dto.AuditArchiveResultDTO;
import com.sme.erp.audit.dto.AuditRetentionDTO;
import com.sme.erp.audit.repository.ActivityLogRepository;
import com.sme.erp.audit.repository.AuditLogRepository;
import com.sme.erp.audit.repository.LoginHistoryRepository;
import com.sme.erp.audit.service.AuditRetentionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditRetentionServiceImpl implements AuditRetentionService {
    private final ActivityLogRepository activityLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final int activityLogRetentionYears;
    private final int securityLogRetentionYears;

    public AuditRetentionServiceImpl(
            ActivityLogRepository activityLogRepository,
            AuditLogRepository auditLogRepository,
            LoginHistoryRepository loginHistoryRepository,
            @Value("${app.audit.activity-log-retention-years:2}") int activityLogRetentionYears,
            @Value("${app.audit.security-log-retention-years:5}") int securityLogRetentionYears) {
        this.activityLogRepository = activityLogRepository;
        this.auditLogRepository = auditLogRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.activityLogRetentionYears = Math.max(activityLogRetentionYears, 1);
        this.securityLogRetentionYears = Math.max(securityLogRetentionYears, 1);
    }

    @Override
    public AuditRetentionDTO policy() {
        return new AuditRetentionDTO(
                activityLogRetentionYears,
                securityLogRetentionYears,
                false,
                "Expired logs are marked archived only; production logs are never automatically deleted.");
    }

    @Override
    @Transactional
    public AuditArchiveResultDTO archiveExpiredLogs() {
        LocalDateTime now = LocalDateTime.now();
        int activity = activityLogRepository.archiveOlderThan(
                now.minusYears(activityLogRetentionYears),
                now,
                "Archived by retention policy after " + activityLogRetentionYears + " years");
        int audit = auditLogRepository.archiveOlderThan(
                now.minusYears(activityLogRetentionYears),
                now,
                "Archived by retention policy after " + activityLogRetentionYears + " years");
        int security = loginHistoryRepository.archiveOlderThan(
                now.minusYears(securityLogRetentionYears),
                now,
                "Archived by retention policy after " + securityLogRetentionYears + " years");
        return new AuditArchiveResultDTO(activity, audit, security);
    }
}
