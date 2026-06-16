package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.StockMovement;
import com.sme.erp.reports.dto.StockMovementReportRowDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {

    List<StockMovement> findByProductId(Long productId);

    List<StockMovement> findByProductIdAndWarehouseIdOrderByCreatedAtAscIdAsc(Long productId, Long warehouseId);

    StockMovement findTopByOrderByIdDesc();

    @Query("""
            select new com.sme.erp.reports.dto.StockMovementReportRowDTO(
                m.createdAt,
                p.productName,
                concat('', m.movementType),
                m.quantity,
                m.referenceNo
            )
            from StockMovement m
            join m.product p
            join m.warehouse w
            where (:warehouseId is null or w.id = :warehouseId)
              and (:productId is null or p.id = :productId)
            order by m.createdAt desc, m.id desc
            """)
    List<StockMovementReportRowDTO> findStockMovementReportRows(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId);

}
