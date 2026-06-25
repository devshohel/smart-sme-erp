package com.sme.erp.audit.controller;

import com.sme.erp.audit.dto.ActivityLogDTO;
import com.sme.erp.audit.dto.AuditLogDTO;
import com.sme.erp.audit.dto.LoginHistoryDTO;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.audit.service.LoginHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    private final ActivityLogService activityLogService;
    private final AuditLogService auditLogService;
    private final LoginHistoryService loginHistoryService;

    public AuditController(ActivityLogService activityLogService, AuditLogService auditLogService, LoginHistoryService loginHistoryService) {
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.loginHistoryService = loginHistoryService;
    }

    @GetMapping("/activity-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and (hasAuthority('ACTIVITY_LOG_VIEW') or hasAuthority('ACTIVITY_VIEW'))")
    public Page<ActivityLogDTO> activityLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        int safeSize = Math.min(Math.max(size, 1), 200);
        return activityLogService.search(
                fromDate,
                toDate,
                username,
                action,
                module,
                search,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/activity-logs/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and hasAuthority('ACTIVITY_LOG_EXPORT')")
    public ResponseEntity<byte[]> exportActivityLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String search) {
        Page<ActivityLogDTO> logs = activityLogService.search(
                fromDate,
                toDate,
                username,
                action,
                module,
                search,
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt")));
        StringBuilder csv = new StringBuilder("createdAt,username,module,action,tableName,entityId,ipAddress,userAgent,details,oldValue,newValue\n");
        logs.forEach(log -> csv.append(csv(log.createdAt()))
                .append(',').append(csv(log.username()))
                .append(',').append(csv(log.module()))
                .append(',').append(csv(log.action()))
                .append(',').append(csv(log.tableName()))
                .append(',').append(csv(log.entityId() != null ? log.entityId() : log.recordId()))
                .append(',').append(csv(log.ipAddress()))
                .append(',').append(csv(log.userAgent()))
                .append(',').append(csv(log.details()))
                .append(',').append(csv(log.oldValue()))
                .append(',').append(csv(log.newValue()))
                .append('\n'));
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activity-logs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and hasAuthority('AUDIT_VIEW')")
    public List<AuditLogDTO> auditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module) {
        return auditLogService.search(fromDate, toDate, username, action, module);
    }

    @GetMapping("/login-history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and hasAuthority('LOGIN_HISTORY_VIEW')")
    public List<LoginHistoryDTO> loginHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action) {
        return loginHistoryService.search(fromDate, toDate, username, action);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
