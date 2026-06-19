package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.Stock;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sme.erp.reports.dto.StockReportRowDTO;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {

	Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<Stock> findWithLockByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("""
            select new com.sme.erp.reports.dto.StockReportRowDTO(
                p.productName,
                p.sku,
                coalesce(c.categoryName, ''),
                coalesce(b.brandName, ''),
                w.name,
                s.quantity,
                coalesce(p.reorderLevel, s.reorderLevel, 0),
                p.status,
                (s.quantity * p.purchasePrice)
            )
            from Stock s
            join s.product p
            join s.warehouse w
            left join p.category c
            left join p.brand b
            where (:warehouseId is null or w.id = :warehouseId)
              and (:productId is null or p.id = :productId)
              and (:categoryId is null or c.id = :categoryId)
              and (:brandId is null or b.id = :brandId)
              and (:keyword is null or lower(p.productName) like lower(concat('%', :keyword, '%'))
                   or lower(p.sku) like lower(concat('%', :keyword, '%'))
                   or lower(w.name) like lower(concat('%', :keyword, '%')))
            order by p.productName asc, w.name asc
            """)
    List<StockReportRowDTO> findStockReportRows(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("keyword") String keyword);

    @Query("select coalesce(sum(s.quantity * p.purchasePrice), 0) from Stock s join s.product p")
    BigDecimal sumInventoryValue();
}
