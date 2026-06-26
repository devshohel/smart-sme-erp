package com.sme.erp.audit.service;

import com.sme.erp.audit.dto.AuditArchiveResultDTO;
import com.sme.erp.audit.dto.AuditRetentionDTO;

public interface AuditRetentionService {
    AuditRetentionDTO policy();

    AuditArchiveResultDTO archiveExpiredLogs();
}
