package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.Stock;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}