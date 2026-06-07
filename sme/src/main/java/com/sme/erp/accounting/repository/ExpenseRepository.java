package com.sme.erp.accounting.repository;

import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
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

    @Query("""
            select coalesce(sum(e.amount), 0) from Expense e
            where e.status = com.sme.erp.accounting.enums.ExpenseStatus.ACTIVE
              and (:fromDate is null or e.expenseDate >= :fromDate)
              and (:toDate is null or e.expenseDate <= :toDate)
            """)
    BigDecimal sumActiveAmountBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
            select e from Expense e
            where e.status = :status and e.paymentMethod = :paymentMethod
            order by e.expenseDate, e.id
            """)
    List<Expense> findByStatusAndPaymentMethodForBook(@Param("status") ExpenseStatus status,
                                                       @Param("paymentMethod") AccountingPaymentMethod paymentMethod);
}
