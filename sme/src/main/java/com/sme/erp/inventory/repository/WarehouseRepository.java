package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.Warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCodeAndIdNot(String warehouseCode, Long id);

    @Query(value = "SELECT * FROM warehouses WHERE is_deleted = true ORDER BY id DESC", nativeQuery = true)
    List<Warehouse> findDeletedWarehouses();

    @Modifying
    @Query(value = "UPDATE warehouses SET is_deleted = false, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);
}
