package com.sme.erp.accounting.repository;

import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    boolean existsByExpenseNo(String expenseNo);

    @Query("select coalesce(max(e.id), 0) from Expense e")
    Long findMaxId();

    @Query("""
            select e from Expense e
            where (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
              and (:categoryId is null or e.category.id = :categoryId)
              and (:paymentMethod is null or e.paymentMethod = :paymentMethod)
            order by e.expenseDate desc, e.id desc
            """)
    List<Expense> search(@Param("fromDate") LocalDate fromDate,
                         @Param("toDate") LocalDate toDate,
                         @Param("categoryId") Long categoryId,
                         @Param("paymentMethod") AccountingPaymentMethod paymentMethod);

    @Query(value = """
            select e from Expense e
            join e.category c
            where (:keyword is null
                    or lower(coalesce(e.expenseNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(e.referenceNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(e.notes, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%')))
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
              and (:categoryId is null or c.id = :categoryId)
              and (:paymentMethod is null or e.paymentMethod = :paymentMethod)
              and (:status is null or e.status = :status)
            """,
            countQuery = """
            select count(e) from Expense e
            join e.category c
            where (:keyword is null
                    or lower(coalesce(e.expenseNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(e.referenceNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(e.notes, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%')))
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
              and (:categoryId is null or c.id = :categoryId)
              and (:paymentMethod is null or e.paymentMethod = :paymentMethod)
              and (:status is null or e.status = :status)
            """)
    Page<Expense> searchPage(@Param("keyword") String keyword,
                             @Param("fromDate") LocalDate fromDate,
                             @Param("toDate") LocalDate toDate,
                             @Param("categoryId") Long categoryId,
                             @Param("paymentMethod") AccountingPaymentMethod paymentMethod,
                             @Param("status") ExpenseStatus status,
                             Pageable pageable);

    @Query("""
            select e from Expense e
            join e.category c
            where e.status = com.sme.erp.accounting.enums.ExpenseStatus.SUBMITTED
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
              and (:categoryId is null or c.id = :categoryId)
              and (:submittedBy is null or lower(coalesce(e.submittedBy, '')) like lower(concat('%', :submittedBy, '%')))
              and (:amountMin is null or e.amount >= :amountMin)
              and (:amountMax is null or e.amount <= :amountMax)
            order by e.submittedAt asc, e.id asc
            """)
    List<Expense> approvalQueue(@Param("fromDate") LocalDate fromDate,
                                @Param("toDate") LocalDate toDate,
                                @Param("categoryId") Long categoryId,
                                @Param("submittedBy") String submittedBy,
                                @Param("amountMin") BigDecimal amountMin,
                                @Param("amountMax") BigDecimal amountMax);

    @Query("""
            select coalesce(sum(e.amount), 0) from Expense e
            where e.status = com.sme.erp.accounting.enums.ExpenseStatus.POSTED
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
            """)
    BigDecimal sumActiveAmountBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
            select coalesce(c.name, 'Uncategorized'), coalesce(sum(e.amount), 0)
            from Expense e
            left join e.category c
            where e.status = com.sme.erp.accounting.enums.ExpenseStatus.POSTED
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
            group by c.name
            order by coalesce(sum(e.amount), 0) desc, coalesce(c.name, 'Uncategorized') asc
            """)
    List<Object[]> summarizePostedByCategory(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
            select e from Expense e
            where e.status = :status and e.paymentMethod = :paymentMethod
            order by e.expenseDate, e.id
            """)
    List<Expense> findByStatusAndPaymentMethodForBook(@Param("status") ExpenseStatus status,
                                                       @Param("paymentMethod") AccountingPaymentMethod paymentMethod);

    @Query("""
            select e from Expense e
            where e.status = :status and e.paymentMethod = :paymentMethod
              and not exists (
                select 1 from JournalEntry j
                where j.sourceType = 'EXPENSE' and j.sourceId = e.id
              )
            order by e.expenseDate, e.id
            """)
    List<Expense> findUnpostedByStatusAndPaymentMethodForBook(@Param("status") ExpenseStatus status,
                                                               @Param("paymentMethod") AccountingPaymentMethod paymentMethod);

    long countByCategoryId(Long categoryId);
}
