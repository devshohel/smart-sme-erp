package com.sme.erp.accounting.repository;
import com.sme.erp.accounting.entity.Budget; import com.sme.erp.accounting.enums.BudgetStatus; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.LocalDate; import java.util.*;
public interface BudgetRepository extends JpaRepository<Budget,Long>{
 @Query("select coalesce(max(b.id),0) from Budget b") Long maxId();
 boolean existsByBudgetNo(String budgetNo);
 @EntityGraph(attributePaths={"account","costCenter"}) @Query("select b from Budget b where (:q is null or lower(b.budgetNo) like lower(concat('%',:q,'%')) or lower(b.account.accountName) like lower(concat('%',:q,'%'))) and (:year is null or b.fiscalYear=:year) and (:status is null or b.status=:status)") Page<Budget> search(@Param("q") String q,@Param("year") Integer year,@Param("status") BudgetStatus status,Pageable p);
 @EntityGraph(attributePaths={"account","costCenter"}) @Query("select b from Budget b where b.id=:id") Optional<Budget> detailed(@Param("id") Long id);
 @EntityGraph(attributePaths={"account","costCenter"}) @Query("select b from Budget b where b.status=com.sme.erp.accounting.enums.BudgetStatus.APPROVED and (:year is null or b.fiscalYear=:year) and (:fromDate is null or b.toDate>=:fromDate) and (:toDate is null or b.fromDate<=:toDate) and (:accountId is null or b.account.id=:accountId) and (:costCenterId is null or b.costCenter.id=:costCenterId)") List<Budget> approved(@Param("year") Integer year,@Param("fromDate") LocalDate fromDate,@Param("toDate") LocalDate toDate,@Param("accountId") Long accountId,@Param("costCenterId") Long costCenterId);
}
