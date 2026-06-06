package com.sme.erp.audit.service;

import com.sme.erp.audit.dto.ActivityLogDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogService {
    void log(String action, String module, String tableName, Long recordId, String details);

    List<ActivityLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module);
}
