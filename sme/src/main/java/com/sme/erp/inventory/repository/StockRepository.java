package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.Stock;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<Stock> findWithLockByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
