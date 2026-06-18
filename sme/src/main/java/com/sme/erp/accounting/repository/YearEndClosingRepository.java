package com.sme.erp.accounting.repository;
import com.sme.erp.accounting.entity.YearEndClosing; import com.sme.erp.accounting.enums.YearEndClosingStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface YearEndClosingRepository extends JpaRepository<YearEndClosing,Long>{ boolean existsByFiscalYearAndStatus(Integer year,YearEndClosingStatus status); boolean existsByFiscalYearAndStatusIn(Integer year,Collection<YearEndClosingStatus> statuses); List<YearEndClosing> findAllByOrderByFiscalYearDesc(); }
