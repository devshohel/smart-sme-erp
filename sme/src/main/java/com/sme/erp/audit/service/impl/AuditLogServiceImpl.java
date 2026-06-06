package com.sme.erp.audit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.erp.audit.dto.AuditLogDTO;
import com.sme.erp.audit.entity.AuditLog;
import com.sme.erp.audit.repository.AuditLogRepository;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.auth.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final CurrentAuditUser currentAuditUser;
    private final ObjectMapper objectMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, CurrentAuditUser currentAuditUser, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.currentAuditUser = currentAuditUser;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void log(String tableName, Long recordId, String oldData, String newData, String action) {
        AuditLog log = new AuditLog();
        log.setUser(currentAuditUser.currentUserOrNull());
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldData(oldData);
        log.setNewData(newData);
        log.setAction(action);
        auditLogRepository.save(log);
    }

    @Override
    public String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"Unable to serialize audit data\"}";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> search(LocalDateTime fromDate, LocalDateTime toDate, String username, String action, String module) {
        return auditLogRepository.search(fromDate, toDate, normalize(username), normalize(action), normalize(module))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AuditLogDTO toDto(AuditLog log) {
        User user = log.getUser();
        return new AuditLogDTO(
                log.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                log.getTableName(),
                log.getRecordId(),
                log.getOldData(),
                log.getNewData(),
                log.getAction(),
                log.getCreatedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
