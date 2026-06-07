package com.sme.erp.accounting.repository;

import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.enums.JournalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    boolean existsByJournalNo(String journalNo);
    boolean existsBySourceTypeAndSourceId(String sourceType, Long sourceId);

    @Query("select coalesce(max(j.id), 0) from JournalEntry j")
    Long findMaxId();

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    @Query("select j from JournalEntry j where j.id = :id")
    Optional<JournalEntry> findWithLinesById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    @Query("select distinct j from JournalEntry j where (:status is null or j.status = :status) order by j.journalDate desc, j.id desc")
    List<JournalEntry> search(@Param("status") JournalStatus status);

    @Query("select j from JournalEntry j where j.sourceType = :sourceType and j.sourceId = :sourceId")
    Optional<JournalEntry> findBySource(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);
}
