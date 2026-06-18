package com.sme.erp.accounting.repository;
import com.sme.erp.accounting.entity.AccountingPeriod; import com.sme.erp.accounting.enums.AccountingPeriodStatus; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.LocalDate; import java.util.*;
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod,Long>{
 List<AccountingPeriod> findAllByOrderByStartDateDesc();
 @Query("select count(p)>0 from AccountingPeriod p where p.status=:status and :date between p.startDate and p.endDate") boolean contains(@Param("date") LocalDate date,@Param("status") AccountingPeriodStatus status);
 @Query("select count(p)>0 from AccountingPeriod p where p.startDate<=:end and p.endDate>=:start") boolean overlaps(@Param("start") LocalDate start,@Param("end") LocalDate end);
}
