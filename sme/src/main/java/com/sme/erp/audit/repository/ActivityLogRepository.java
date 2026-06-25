package com.sme.erp.audit.repository;

import com.sme.erp.audit.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    @Query("""
            select l from ActivityLog l
            left join fetch l.user u
            where (:fromDate is null or l.createdAt >= :fromDate)
              and (:toDate is null or l.createdAt <= :toDate)
              and (:username is null or lower(u.username) like lower(concat('%', :username, '%')))
              and (:action is null or lower(l.action) like lower(concat('%', :action, '%')))
              and (:module is null or lower(l.module) like lower(concat('%', :module, '%')))
            order by l.createdAt desc
            """)
    List<ActivityLog> search(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("username") String username,
            @Param("action") String action,
            @Param("module") String module);

    @Query(value = """
            select l from ActivityLog l
            left join fetch l.user u
            where (:fromDate is null or l.createdAt >= :fromDate)
              and (:toDate is null or l.createdAt <= :toDate)
              and (:username is null or lower(coalesce(l.username, u.username)) like lower(concat('%', :username, '%')))
              and (:action is null or lower(l.action) like lower(concat('%', :action, '%')))
              and (:module is null or lower(l.module) like lower(concat('%', :module, '%')))
              and (:search is null
                   or lower(coalesce(l.details, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.oldValue, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.newValue, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.ipAddress, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.userAgent, '')) like lower(concat('%', :search, '%')))
            """,
            countQuery = """
            select count(l) from ActivityLog l
            left join l.user u
            where (:fromDate is null or l.createdAt >= :fromDate)
              and (:toDate is null or l.createdAt <= :toDate)
              and (:username is null or lower(coalesce(l.username, u.username)) like lower(concat('%', :username, '%')))
              and (:action is null or lower(l.action) like lower(concat('%', :action, '%')))
              and (:module is null or lower(l.module) like lower(concat('%', :module, '%')))
              and (:search is null
                   or lower(coalesce(l.details, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.oldValue, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.newValue, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.ipAddress, '')) like lower(concat('%', :search, '%'))
                   or lower(coalesce(l.userAgent, '')) like lower(concat('%', :search, '%')))
            """)
    Page<ActivityLog> searchPage(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("username") String username,
            @Param("action") String action,
            @Param("module") String module,
            @Param("search") String search,
            Pageable pageable);
}
