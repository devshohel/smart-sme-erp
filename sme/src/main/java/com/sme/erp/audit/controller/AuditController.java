package com.sme.erp.audit.controller;

import com.sme.erp.audit.dto.ActivityLogDTO;
import com.sme.erp.audit.dto.AuditLogDTO;
import com.sme.erp.audit.dto.LoginHistoryDTO;
import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.audit.service.AuditLogService;
import com.sme.erp.audit.service.LoginHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@CrossOrigin(origins = "*")
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','AUDITOR') and hasAuthority('ACTIVITY_VIEW')")
    public List<ActivityLogDTO> activityLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module) {
        return activityLogService.search(fromDate, toDate, username, action, module);
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
}
