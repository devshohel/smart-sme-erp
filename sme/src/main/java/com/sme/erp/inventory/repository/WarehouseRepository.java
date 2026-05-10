package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.Warehouse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCodeAndIdNot(String warehouseCode, Long id);
}
