package com.sme.erp.audit.service;

import com.sme.erp.audit.dto.AuditLogDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    void log(String tableName, Long recordId, String oldData, String newData, String action);

    String toJson(Object value);

    List<AuditLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module);
}
