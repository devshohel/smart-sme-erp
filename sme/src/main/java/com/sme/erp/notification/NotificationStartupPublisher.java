package com.sme.erp.notification;

import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import com.sme.erp.notification.service.NotificationService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationStartupPublisher {
    private final NotificationService notificationService;

    public NotificationStartupPublisher(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishMigrationCompleted() {
        notificationService.notifyGlobalOnce(
                "Database migration completed",
                "Application startup completed and Flyway migrations are up to date.",
                NotificationType.SUCCESS,
                NotificationSeverity.LOW,
                "DATABASE_MIGRATION",
                11L,
                "/settings/activity-logs");
    }
}
