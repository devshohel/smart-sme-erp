package com.sme.erp.audit.service.impl;

import com.sme.erp.audit.dto.ActivityLogDTO;
import com.sme.erp.audit.entity.ActivityLog;
import com.sme.erp.audit.repository.ActivityLogRepository;
import com.sme.erp.audit.service.ActivityLogInvocationContext;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditRequestContext;
import com.sme.erp.auth.entity.User;
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

    public ActivityLogServiceImpl(
            ActivityLogRepository activityLogRepository,
            CurrentAuditUser currentAuditUser,
            AuditRequestContext requestContext) {
        this.activityLogRepository = activityLogRepository;
        this.currentAuditUser = currentAuditUser;
        this.requestContext = requestContext;
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
        log.setModule(module);
        log.setTableName(tableName);
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
    public Page<ActivityLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module, String search, Pageable pageable) {
        return activityLogRepository.searchPage(fromDate, toDate, normalize(username), normalize(action), normalize(module), normalize(search), pageable)
                .map(this::toDto);
    }

    private ActivityLogDTO toDto(ActivityLog log) {
        User user = log.getUser();
        String username = log.getUsername() != null ? log.getUsername() : (user != null ? user.getUsername() : null);
        return new ActivityLogDTO(
                log.getId(),
                user != null ? user.getId() : null,
                username,
                log.getAction(),
                log.getModule(),
                log.getTableName(),
                log.getRecordId(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getDetails(),
                log.getOldValue(),
                log.getNewValue(),
                log.getCreatedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
