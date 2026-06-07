package com.sme.erp.accounting.repository;

import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.JournalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {
    @EntityGraph(attributePaths = {"journalEntry", "account"})
    @Query("""
            select l from JournalEntryLine l
            where l.account.id = :accountId and l.journalEntry.status = :status
            order by l.journalEntry.journalDate, l.journalEntry.id, l.id
            """)
    List<JournalEntryLine> findBookLines(@Param("accountId") Long accountId, @Param("status") JournalStatus status);
}
