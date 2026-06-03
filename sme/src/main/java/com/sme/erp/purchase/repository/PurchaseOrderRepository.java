package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findTopByOrderByIdDesc();
    boolean existsByPurchaseCode(String purchaseCode);
    boolean existsByPurchaseCodeAndIdNot(String purchaseCode, Long id);
}
