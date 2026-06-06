package com.sme.erp.audit.service.impl;

import com.sme.erp.audit.dto.ActivityLogDTO;
import com.sme.erp.audit.entity.ActivityLog;
import com.sme.erp.audit.repository.ActivityLogRepository;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditRequestContext;
import com.sme.erp.auth.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final CurrentAuditUser currentAuditUser;
    private final AuditRequestContext requestContext;

    public ActivityLogServiceImpl(
            ActivityLogRepository activityLogRepository,
            CurrentAuditUser currentAuditUser,
            AuditRequestContext requestContext) {
        this.activityLogRepository = activityLogRepository;
        this.currentAuditUser = currentAuditUser;
        this.requestContext = requestContext;
    }

    @Override
    @Transactional
    public void log(String action, String module, String tableName, Long recordId, String details) {
        ActivityLog log = new ActivityLog();
        log.setUser(currentAuditUser.currentUserOrNull());
        log.setAction(action);
        log.setModule(module);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setIpAddress(requestContext.ipAddress());
        log.setDetails(details);
        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module) {
        return activityLogRepository.search(fromDate, toDate, normalize(username), normalize(action), normalize(module))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ActivityLogDTO toDto(ActivityLog log) {
        User user = log.getUser();
        return new ActivityLogDTO(
                log.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                log.getAction(),
                log.getModule(),
                log.getTableName(),
                log.getRecordId(),
                log.getIpAddress(),
                log.getDetails(),
                log.getCreatedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
