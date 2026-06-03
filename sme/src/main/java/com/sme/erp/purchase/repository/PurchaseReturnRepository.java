package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.PurchaseReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {
    Optional<PurchaseReturn> findTopByOrderByIdDesc();
    boolean existsByReturnCode(String returnCode);
    boolean existsByReturnCodeAndIdNot(String returnCode, Long id);
}
