package com.sme.erp.audit.repository;

import com.sme.erp.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("""
            select l from AuditLog l
            left join fetch l.user u
            where (:fromDate is null or l.createdAt >= :fromDate)
              and (:toDate is null or l.createdAt <= :toDate)
              and (:username is null or lower(u.username) like lower(concat('%', :username, '%')))
              and (:action is null or lower(l.action) like lower(concat('%', :action, '%')))
              and (:module is null or lower(l.tableName) like lower(concat('%', :module, '%')))
            order by l.createdAt desc
            """)
    List<AuditLog> search(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("username") String username,
            @Param("action") String action,
            @Param("module") String module);
}
