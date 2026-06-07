package com.sme.erp.accounting.repository;

import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select c from ExpenseCategory c where (:status is null or c.status = :status) order by c.name")
    List<ExpenseCategory> search(@Param("status") Status status);
}
