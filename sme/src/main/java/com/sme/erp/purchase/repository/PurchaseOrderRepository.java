package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.supplier.dto.SupplierPurchaseSummaryDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.math.BigDecimal;
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
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
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
            where p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
            )
            group by s.id, s.name
            having coalesce(sum(p.dueAmount), 0) > 0
            order by coalesce(sum(p.dueAmount), 0) desc
            """)
    List<SupplierDueReportRowDTO> findSupplierDueReportRows();

    @Query("""
            select coalesce(sum(p.dueAmount), 0)
            from PurchaseOrder p
            where p.supplier.id = :supplierId
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
            """)
    BigDecimal calculateSupplierDue(@Param("supplierId") Long supplierId);

    @Query("""
            select new com.sme.erp.supplier.dto.SupplierPurchaseSummaryDTO(
                p.purchaseCode,
                p.purchaseDate,
                p.netTotal,
                p.paidAmount,
                p.dueAmount,
                p.status
            )
            from PurchaseOrder p
            where p.supplier.id = :supplierId
            order by p.purchaseDate desc, p.id desc
            """)
    List<SupplierPurchaseSummaryDTO> findRecentPurchaseSummariesBySupplierId(@Param("supplierId") Long supplierId, Pageable pageable);

    @Query("""
            select p
            from PurchaseOrder p
            where p.supplier.id = :supplierId
              and coalesce(p.dueAmount, 0) > 0
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
            order by p.purchaseDate asc, p.id asc
            """)
    List<PurchaseOrder> findUnpaidBySupplierIdOrderByPurchaseDateAscIdAsc(@Param("supplierId") Long supplierId);

    @Query("""
            select p
            from PurchaseOrder p
            where p.supplier.id = :supplierId
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
              and (:fromDate is null or p.purchaseDate >= :fromDate)
              and (:toDate is null or p.purchaseDate < :toDate)
            order by p.purchaseDate asc, p.id asc
            """)
    List<PurchaseOrder> findPostedBySupplierForLedger(@Param("supplierId") Long supplierId,
                                                       @Param("fromDate") LocalDateTime fromDate,
                                                       @Param("toDate") LocalDateTime toDate);

    @Query("""
            select p
            from PurchaseOrder p
            where (:supplierId is null or p.supplier.id = :supplierId)
              and coalesce(p.dueAmount, 0) > 0
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
              and (:fromDate is null or p.purchaseDate >= :fromDate)
              and (:toDate is null or p.purchaseDate < :toDate)
            order by p.supplier.name asc, p.purchaseDate asc, p.id asc
            """)
    List<PurchaseOrder> findDuePurchasesForAging(@Param("supplierId") Long supplierId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate);
}
