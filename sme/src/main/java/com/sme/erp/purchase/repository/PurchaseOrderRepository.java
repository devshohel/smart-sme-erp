package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findTopByOrderByIdDesc();
    boolean existsByPurchaseCode(String purchaseCode);
    boolean existsByPurchaseCodeAndIdNot(String purchaseCode, Long id);

    @Query("""
            select new com.sme.erp.reports.dto.PurchaseReportRowDTO(
                p.purchaseCode,
                s.name,
                p.purchaseDate,
                p.netTotal,
                p.paidAmount,
                p.dueAmount,
                p.status
            )
            from PurchaseOrder p
            join p.supplier s
            where (:startDate is null or p.purchaseDate >= :startDate)
              and (:endDate is null or p.purchaseDate < :endDate)
              and (:supplierId is null or s.id = :supplierId)
              and p.status <> com.sme.erp.purchase.enums.PurchaseStatus.CANCELLED
            order by p.purchaseDate desc, p.id desc
            """)
    List<PurchaseReportRowDTO> findPurchaseReportRows(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("supplierId") Long supplierId);

    @Query("""
            select new com.sme.erp.reports.dto.SupplierDueReportRowDTO(
                s.name,
                coalesce(sum(p.netTotal), 0),
                coalesce(sum(p.paidAmount), 0),
                coalesce(sum(p.dueAmount), 0)
            )
            from PurchaseOrder p
            join p.supplier s
            where p.status <> com.sme.erp.purchase.enums.PurchaseStatus.CANCELLED
            group by s.id, s.name
            having coalesce(sum(p.dueAmount), 0) > 0
            order by coalesce(sum(p.dueAmount), 0) desc
            """)
    List<SupplierDueReportRowDTO> findSupplierDueReportRows();
}
