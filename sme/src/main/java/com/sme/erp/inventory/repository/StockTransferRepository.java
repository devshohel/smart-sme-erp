package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long>, JpaSpecificationExecutor<StockTransfer> {
    StockTransfer findTopByOrderByIdDesc();
}
