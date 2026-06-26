package com.sme.erp.notification.repository;

import com.sme.erp.notification.entity.Notification;
import com.sme.erp.notification.enums.NotificationSeverity;
import com.sme.erp.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
            select n from Notification n
            where (:userId is null or n.targetUser.id is null or n.targetUser.id = :userId)
              and (:roleId is null or n.targetRole.id is null or n.targetRole.id = :roleId)
              and (:read is null or n.read = :read)
              and (:type is null or n.type = :type)
              and (:severity is null or n.severity = :severity)
              and (:fromDate is null or n.createdAt >= :fromDate)
              and (:toDate is null or n.createdAt < :toDate)
            """)
    Page<Notification> searchVisible(@Param("userId") Long userId,
                                     @Param("roleId") Long roleId,
                                     @Param("read") Boolean read,
                                     @Param("type") NotificationType type,
                                     @Param("severity") NotificationSeverity severity,
                                     @Param("fromDate") LocalDateTime fromDate,
                                     @Param("toDate") LocalDateTime toDate,
                                     Pageable pageable);

    @Query("""
            select count(n) from Notification n
            where n.read = false
              and (:userId is null or n.targetUser.id is null or n.targetUser.id = :userId)
              and (:roleId is null or n.targetRole.id is null or n.targetRole.id = :roleId)
            """)
    long countUnreadVisible(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Modifying
    @Query("""
            update Notification n set n.read = true, n.readAt = :readAt
            where n.read = false
              and (:userId is null or n.targetUser.id is null or n.targetUser.id = :userId)
              and (:roleId is null or n.targetRole.id is null or n.targetRole.id = :roleId)
            """)
    int markAllReadVisible(@Param("userId") Long userId, @Param("roleId") Long roleId, @Param("readAt") LocalDateTime readAt);

    boolean existsByTitleAndEntityTypeAndEntityId(String title, String entityType, Long entityId);
}
