package com.sme.erp.audit.repository;

import com.sme.erp.audit.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    @Query("""
            select l from LoginHistory l
            left join fetch l.user u
            where (:fromDate is null or l.createdAt >= :fromDate)
              and (:toDate is null or l.createdAt <= :toDate)
              and (:username is null or lower(l.username) like lower(concat('%', :username, '%')))
              and (:action is null or lower(l.status) like lower(concat('%', :action, '%')))
            order by l.createdAt desc
            """)
    List<LoginHistory> search(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("username") String username,
            @Param("action") String action);

    @Modifying
    @Query("""
            update LoginHistory l
            set l.archivedAt = :archivedAt,
                l.archiveReason = :archiveReason
            where l.archivedAt is null
              and l.createdAt < :cutoff
            """)
    int archiveOlderThan(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("archivedAt") LocalDateTime archivedAt,
            @Param("archiveReason") String archiveReason);
}
