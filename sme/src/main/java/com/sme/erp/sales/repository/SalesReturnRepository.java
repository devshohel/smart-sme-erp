package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesReturn;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {
    Optional<SalesReturn> findTopByOrderByIdDesc();
    boolean existsByReturnCode(String returnCode);
    boolean existsByReturnCodeAndIdNot(String returnCode, Long id);
    List<SalesReturn> findByCustomerIdOrderByReturnDateDescIdDesc(Long customerId, Pageable pageable);
}
