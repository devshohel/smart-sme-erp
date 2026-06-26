package com.sme.erp.audit.service.impl;

import com.sme.erp.audit.dto.ActivityLogDTO;
import com.sme.erp.audit.entity.ActivityLog;
import com.sme.erp.audit.repository.ActivityLogRepository;
import com.sme.erp.audit.service.ActivityLogInvocationContext;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditRequestContext;
import com.sme.erp.auth.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final CurrentAuditUser currentAuditUser;
    private final AuditRequestContext requestContext;
    private final int activityLogRetentionYears;

    public ActivityLogServiceImpl(
            ActivityLogRepository activityLogRepository,
            CurrentAuditUser currentAuditUser,
            AuditRequestContext requestContext,
            @Value("${app.audit.activity-log-retention-years:2}") int activityLogRetentionYears) {
        this.activityLogRepository = activityLogRepository;
        this.currentAuditUser = currentAuditUser;
        this.requestContext = requestContext;
        this.activityLogRetentionYears = activityLogRetentionYears;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String module, String tableName, Long recordId, String details) {
        log(action, module, tableName, recordId, null, null, details);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String module, String tableName, Long entityId, String oldValue, String newValue, String details) {
        ActivityLogInvocationContext.markLogged();
        User user = currentAuditUser.currentUserOrNull();
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setUsername(user != null ? user.getUsername() : null);
        log.setAction(action);
        log.setActionType(actionType(action));
        log.setModule(module);
        log.setTableName(tableName);
        log.setEntityName(entityName(tableName, module));
        log.setRecordId(entityId);
        log.setEntityId(entityId);
        log.setIpAddress(requestContext.ipAddress());
        log.setUserAgent(requestContext.userAgent());
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
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

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module, Long entityId, String search, Pageable pageable) {
        return activityLogRepository.searchPage(fromDate, toDate, normalize(username), normalize(action), normalize(module), entityId, normalize(search), pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogDTO> entityHistory(String entityName, Long entityId) {
        return activityLogRepository.findEntityHistory(normalizeRequired(entityName, "Entity name"), entityId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public int archiveExpiredActivityLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(Math.max(activityLogRetentionYears, 1));
        return activityLogRepository.archiveOlderThan(cutoff, LocalDateTime.now(),
                "Archived by retention policy after " + activityLogRetentionYears + " years");
    }

    private ActivityLogDTO toDto(ActivityLog log) {
        User user = log.getUser();
        String username = log.getUsername() != null ? log.getUsername() : (user != null ? user.getUsername() : null);
        return new ActivityLogDTO(
                log.getId(),
                user != null ? user.getId() : null,
                username,
                log.getAction(),
                log.getActionType(),
                log.getModule(),
                log.getTableName(),
                log.getEntityName(),
                log.getRecordId(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getDetails(),
                log.getOldValue(),
                log.getNewValue(),
                log.getCreatedAt(),
                log.getArchivedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeRequired(String value, String field) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalized;
    }

    private String actionType(String action) {
        String value = normalize(action);
        if (value == null) {
            return "UNKNOWN";
        }
        String upper = value.toUpperCase();
        for (String type : List.of("CREATE", "UPDATE", "DELETE", "RESTORE", "APPROVE", "REJECT", "POST", "CANCEL", "REVERSE", "LOGIN", "LOGOUT", "PASSWORD")) {
            if (upper.contains(type)) {
                return "PASSWORD".equals(type) ? "PASSWORD_CHANGE" : type;
            }
        }
        return upper;
    }

    private String entityName(String tableName, String module) {
        String source = normalize(tableName);
        if (source == null) {
            source = normalize(module);
        }
        if (source == null) {
            return null;
        }
        String[] parts = source.replace('-', '_').split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return builder.toString();
    }
}
