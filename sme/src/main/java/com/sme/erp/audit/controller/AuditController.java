package com.sme.erp.audit.controller;

import com.sme.erp.audit.dto.ActivityLogDTO;
import com.sme.erp.audit.dto.AuditArchiveResultDTO;
import com.sme.erp.audit.dto.AuditRetentionDTO;
import com.sme.erp.audit.dto.AuditLogDTO;
import com.sme.erp.audit.dto.LoginHistoryDTO;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.audit.service.AuditRetentionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final AuditRetentionService auditRetentionService;
    private final LoginHistoryService loginHistoryService;

    public AuditController(
            ActivityLogService activityLogService,
            AuditLogService auditLogService,
            AuditRetentionService auditRetentionService,
            LoginHistoryService loginHistoryService) {
        this.activityLogService = activityLogService;
        this.auditLogService = auditLogService;
        this.auditRetentionService = auditRetentionService;
        this.loginHistoryService = loginHistoryService;
    }

    @GetMapping("/activity-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and (hasAuthority('AUDIT_VIEW') or hasAuthority('ACTIVITY_LOG_VIEW') or hasAuthority('ACTIVITY_VIEW'))")
    public Page<ActivityLogDTO> activityLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long entityId,
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
                entityId,
                search,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/activity-logs/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and (hasAuthority('AUDIT_EXPORT') or hasAuthority('ACTIVITY_LOG_EXPORT'))")
    public ResponseEntity<byte[]> exportActivityLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String search) {
        Page<ActivityLogDTO> logs = activityLogService.search(fromDate, toDate, username, action, module, entityId, search,
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt")));
        byte[] body = activityLogDelimited(logs, ",").getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activity-logs.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/activity-logs/export-excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and (hasAuthority('AUDIT_EXPORT') or hasAuthority('ACTIVITY_LOG_EXPORT'))")
    public ResponseEntity<byte[]> exportActivityLogsExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String search) {
        Page<ActivityLogDTO> logs = activityLogService.search(fromDate, toDate, username, action, module, entityId, search,
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt")));
        byte[] body = activityLogDelimited(logs, "\t").getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activity-logs.xls")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel;charset=UTF-8"))
                .body(body);
    }

    @GetMapping("/activity-logs/history/{entityName}/{entityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and (hasAuthority('AUDIT_VIEW') or hasAuthority('ACTIVITY_VIEW') or hasAuthority('ACTIVITY_LOG_VIEW'))")
    public List<ActivityLogDTO> entityHistory(@PathVariable String entityName, @PathVariable Long entityId) {
        return activityLogService.entityHistory(entityName, entityId);
    }

    @GetMapping("/retention")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and hasAuthority('AUDIT_VIEW')")
    public AuditRetentionDTO retentionPolicy() {
        return auditRetentionService.policy();
    }

    @PostMapping("/activity-logs/archive-expired")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') and hasAuthority('AUDIT_EXPORT')")
    public java.util.Map<String, Integer> archiveExpiredActivityLogs() {
        return java.util.Map.of("archived", activityLogService.archiveExpiredActivityLogs());
    }

    @PostMapping("/archive-expired")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN') and hasAuthority('AUDIT_EXPORT')")
    public AuditArchiveResultDTO archiveExpiredLogs() {
        return auditRetentionService.archiveExpiredLogs();
    }

    private String activityLogDelimited(Page<ActivityLogDTO> logs, String delimiter) {
        String header = "\t".equals(delimiter)
                ? "createdAt\tusername\tmodule\taction\tentityName\tentityId\tipAddress\tuserAgent\tdetails\toldValue\tnewValue\n"
                : "createdAt,username,module,action,entityName,entityId,ipAddress,userAgent,details,oldValue,newValue\n";
        StringBuilder csv = new StringBuilder(header);
        String separator = delimiter;
        logs.forEach(log -> csv.append(cell(log.createdAt(), delimiter))
                .append(separator).append(cell(log.username(), delimiter))
                .append(separator).append(cell(log.module(), delimiter))
                .append(separator).append(cell(log.action(), delimiter))
                .append(separator).append(cell(log.entityName() != null ? log.entityName() : log.tableName(), delimiter))
                .append(separator).append(cell(log.entityId() != null ? log.entityId() : log.recordId(), delimiter))
                .append(separator).append(cell(log.ipAddress(), delimiter))
                .append(separator).append(cell(log.userAgent(), delimiter))
                .append(separator).append(cell(log.details(), delimiter))
                .append(separator).append(cell(log.oldValue(), delimiter))
                .append(separator).append(cell(log.newValue(), delimiter))
                .append('\n'));
        return csv.toString();
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

    private String cell(Object value, String delimiter) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).replace("\"", "\"\"").replace("\r", " ").replace("\n", " ");
        if ("\t".equals(delimiter)) {
            return text;
        }
        return "\"" + text + "\"";
    }
}
