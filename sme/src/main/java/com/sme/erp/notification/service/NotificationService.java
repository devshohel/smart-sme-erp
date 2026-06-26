package com.sme.erp.notification.service;

import com.sme.erp.notification.dto.NotificationDTO;
import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface NotificationService {
    Page<NotificationDTO> getNotifications(Boolean read, NotificationType type, NotificationSeverity severity,
                                           LocalDate fromDate, LocalDate toDate, int page, int size);

    long unreadCount();

    NotificationDTO markAsRead(Long id);

    int markAllAsRead();

    void delete(Long id);

    NotificationDTO notifyGlobal(String title, String message, NotificationType type, NotificationSeverity severity,
                                 String entityType, Long entityId, String actionUrl);

    NotificationDTO notifyUser(Long userId, String title, String message, NotificationType type, NotificationSeverity severity,
                               String entityType, Long entityId, String actionUrl);

    NotificationDTO notifyRole(Long roleId, String title, String message, NotificationType type, NotificationSeverity severity,
                               String entityType, Long entityId, String actionUrl);

    void notifyGlobalOnce(String title, String message, NotificationType type, NotificationSeverity severity,
                          String entityType, Long entityId, String actionUrl);
}
