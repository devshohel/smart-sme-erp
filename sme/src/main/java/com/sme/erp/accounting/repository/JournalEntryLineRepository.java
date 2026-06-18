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

    @EntityGraph(attributePaths = {"journalEntry", "account"})
    @Query("""
            select l from JournalEntryLine l
            where l.account.id = :accountId
              and l.journalEntry.status = com.sme.erp.accounting.enums.JournalStatus.POSTED
              and (:fromDate is null or l.journalEntry.journalDate >= :fromDate)
              and (:toDate is null or l.journalEntry.journalDate <= :toDate)
            order by l.journalEntry.journalDate, l.journalEntry.id, l.id
            """)
    List<JournalEntryLine> findBookLines(@Param("accountId") Long accountId,
                                         @Param("fromDate") java.time.LocalDate fromDate,
                                         @Param("toDate") java.time.LocalDate toDate);

    @Query("""
            select coalesce(sum(l.debit - l.credit), 0) from JournalEntryLine l
            where l.account.id = :accountId
              and l.journalEntry.status = com.sme.erp.accounting.enums.JournalStatus.POSTED
              and :fromDate is not null and l.journalEntry.journalDate < :fromDate
            """)
    java.math.BigDecimal openingBalance(@Param("accountId") Long accountId,
                                        @Param("fromDate") java.time.LocalDate fromDate);

    @EntityGraph(attributePaths = {"journalEntry", "account"})
    @Query("""
            select l from JournalEntryLine l
            where l.journalEntry.status = com.sme.erp.accounting.enums.JournalStatus.POSTED
              and (:accountId is null or l.account.id = :accountId)
              and (:fromDate is null or l.journalEntry.journalDate >= :fromDate)
              and (:toDate is null or l.journalEntry.journalDate <= :toDate)
            order by l.journalEntry.journalDate, l.journalEntry.id, l.id
            """)
    List<JournalEntryLine> findPostedLedgerLines(@Param("accountId") Long accountId,
                                                  @Param("fromDate") java.time.LocalDate fromDate,
                                                  @Param("toDate") java.time.LocalDate toDate);

    @Query("""
            select coalesce(sum(l.debit), 0)
            from JournalEntryLine l
            where l.journalEntry.status = com.sme.erp.accounting.enums.JournalStatus.POSTED
              and l.account.id = :accountId
              and (:fromDate is null or l.journalEntry.journalDate >= :fromDate)
              and (:toDate is null or l.journalEntry.journalDate <= :toDate)
            """)
    java.math.BigDecimal sumDebit(@Param("accountId") Long accountId,
                                  @Param("fromDate") java.time.LocalDate fromDate,
                                  @Param("toDate") java.time.LocalDate toDate);

    @Query("""
            select coalesce(sum(l.credit), 0)
            from JournalEntryLine l
            where l.journalEntry.status = com.sme.erp.accounting.enums.JournalStatus.POSTED
              and l.account.id = :accountId
              and (:fromDate is null or l.journalEntry.journalDate >= :fromDate)
              and (:toDate is null or l.journalEntry.journalDate <= :toDate)
            """)
    java.math.BigDecimal sumCredit(@Param("accountId") Long accountId,
                                   @Param("fromDate") java.time.LocalDate fromDate,
                                   @Param("toDate") java.time.LocalDate toDate);
}
