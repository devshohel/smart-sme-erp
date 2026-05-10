package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
}