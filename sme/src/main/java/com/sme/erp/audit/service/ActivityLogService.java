package com.sme.erp.audit.service;

import com.sme.erp.audit.dto.ActivityLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogService {
    void log(String action, String module, String tableName, Long recordId, String details);

    void log(String action, String module, String tableName, Long entityId, String oldValue, String newValue, String details);

    List<ActivityLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module);

    Page<ActivityLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module, String search, Pageable pageable);
}
