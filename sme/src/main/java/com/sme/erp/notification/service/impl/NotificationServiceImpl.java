package com.sme.erp.notification.service.impl;

import com.sme.erp.audit.service.ActivityLogService;
import com.sme.erp.auth.entity.Role;
import com.sme.erp.auth.entity.User;
import com.sme.erp.auth.repository.RoleRepository;
import com.sme.erp.auth.repository.UserRepository;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.notification.dto.NotificationDTO;
import com.sme.erp.notification.entity.Notification;
import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import com.sme.erp.notification.repository.NotificationRepository;
import com.sme.erp.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ActivityLogService activityLogService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            ActivityLogService activityLogService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(Boolean read, NotificationType type, NotificationSeverity severity,
                                                  LocalDate fromDate, LocalDate toDate, int page, int size) {
        User current = currentUser();
        return notificationRepository.searchVisible(
                        current.getId(),
                        current.getRole() != null ? current.getRole().getId() : null,
                        read,
                        type,
                        severity,
                        fromDate == null ? null : fromDate.atStartOfDay(),
                        toDate == null ? null : toDate.plusDays(1).atStartOfDay(),
                        PageRequest.of(Math.max(page, 0), safeSize(size), Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))))
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount() {
        User current = currentUser();
        return notificationRepository.countUnreadVisible(current.getId(), current.getRole() != null ? current.getRole().getId() : null);
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(Long id) {
        User current = currentUser();
        Notification notification = findVisible(id, current);
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            activityLogService.log("NOTIFICATION_READ", "NOTIFICATION", "notifications", id, "Read notification " + notification.getTitle());
        }
        return toDto(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead() {
        User current = currentUser();
        int updated = notificationRepository.markAllReadVisible(
                current.getId(),
                current.getRole() != null ? current.getRole().getId() : null,
                LocalDateTime.now());
        activityLogService.log("NOTIFICATION_MARK_ALL_READ", "NOTIFICATION", "notifications", null, "Marked " + updated + " notifications as read");
        return updated;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User current = currentUser();
        Notification notification = findVisible(id, current);
        notificationRepository.delete(notification);
        activityLogService.log("NOTIFICATION_DELETE", "NOTIFICATION", "notifications", id, "Deleted notification " + notification.getTitle());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationDTO notifyGlobal(String title, String message, NotificationType type, NotificationSeverity severity,
                                        String entityType, Long entityId, String actionUrl) {
        return toDto(notificationRepository.save(build(title, message, type, severity, entityType, entityId, actionUrl, null, null)));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationDTO notifyUser(Long userId, String title, String message, NotificationType type, NotificationSeverity severity,
                                      String entityType, Long entityId, String actionUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification target user not found with id: " + userId));
        return toDto(notificationRepository.save(build(title, message, type, severity, entityType, entityId, actionUrl, user, null)));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationDTO notifyRole(Long roleId, String title, String message, NotificationType type, NotificationSeverity severity,
                                      String entityType, Long entityId, String actionUrl) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification target role not found with id: " + roleId));
        return toDto(notificationRepository.save(build(title, message, type, severity, entityType, entityId, actionUrl, null, role)));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyGlobalOnce(String title, String message, NotificationType type, NotificationSeverity severity,
                                 String entityType, Long entityId, String actionUrl) {
        if (!notificationRepository.existsByTitleAndEntityTypeAndEntityId(title, entityType, entityId)) {
            notificationRepository.save(build(title, message, type, severity, entityType, entityId, actionUrl, null, null));
        }
    }

    private Notification build(String title, String message, NotificationType type, NotificationSeverity severity,
                               String entityType, Long entityId, String actionUrl, User targetUser, Role targetRole) {
        Notification notification = new Notification();
        notification.setTitle(RequestValueUtils.normalizeRequired(title, "Notification title"));
        notification.setMessage(RequestValueUtils.normalizeRequired(message, "Notification message"));
        notification.setType(type == null ? NotificationType.INFO : type);
        notification.setSeverity(severity == null ? NotificationSeverity.LOW : severity);
        notification.setEntityType(RequestValueUtils.normalize(entityType));
        notification.setEntityId(entityId);
        notification.setActionUrl(RequestValueUtils.normalize(actionUrl));
        notification.setCreatedBy(currentUsernameOrSystem());
        notification.setTargetUser(targetUser);
        notification.setTargetRole(targetRole);
        return notification;
    }

    private Notification findVisible(Long id, User current) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        Long currentRoleId = current.getRole() != null ? current.getRole().getId() : null;
        boolean userMatches = notification.getTargetUser() == null || notification.getTargetUser().getId().equals(current.getId());
        boolean roleMatches = notification.getTargetRole() == null || notification.getTargetRole().getId().equals(currentRoleId);
        if (!userMatches || !roleMatches) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        return notification;
    }

    private User currentUser() {
        String username = currentUsernameOrSystem();
        if ("SYSTEM".equals(username)) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private String currentUsernameOrSystem() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            return "SYSTEM";
        }
        return authentication.getName();
    }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setSeverity(notification.getSeverity());
        dto.setEntityType(notification.getEntityType());
        dto.setEntityId(notification.getEntityId());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setCreatedBy(notification.getCreatedBy());
        dto.setRead(notification.isRead());
        dto.setReadAt(notification.getReadAt());
        dto.setTargetUserId(notification.getTargetUser() != null ? notification.getTargetUser().getId() : null);
        dto.setTargetRoleId(notification.getTargetRole() != null ? notification.getTargetRole().getId() : null);
        dto.setActionUrl(notification.getActionUrl());
        return dto;
    }

    private int safeSize(int size) {
        return size == 25 || size == 50 || size == 100 ? size : 10;
    }
}
