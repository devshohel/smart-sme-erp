package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.PurchaseByProductRowDTO;
import com.sme.erp.reports.dto.SupplierPurchaseRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.supplier.dto.SupplierPurchaseSummaryDTO;
import com.sme.erp.purchase.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

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
                w.name,
                p.purchaseDate,
                p.netTotal,
                p.paidAmount,
                p.dueAmount,
                p.status
            )
            from PurchaseOrder p
            join p.supplier s
            join p.warehouse w
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
            select new com.sme.erp.reports.dto.PurchaseReportRowDTO(
                p.purchaseCode,
                s.name,
                w.name,
                p.purchaseDate,
                p.netTotal,
                p.paidAmount,
                p.dueAmount,
                p.status
            )
            from PurchaseOrder p
            join p.supplier s
            join p.warehouse w
            where (:startDate is null or p.purchaseDate >= :startDate)
              and (:endDate is null or p.purchaseDate < :endDate)
              and (:supplierId is null or s.id = :supplierId)
              and (:warehouseId is null or w.id = :warehouseId)
              and (:keyword is null or lower(p.purchaseCode) like lower(concat('%', :keyword, '%'))
                   or lower(s.name) like lower(concat('%', :keyword, '%'))
                   or lower(w.name) like lower(concat('%', :keyword, '%')))
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
            order by p.purchaseDate desc, p.id desc
            """)
    List<PurchaseReportRowDTO> findPurchaseDetailRows(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      @Param("supplierId") Long supplierId,
                                                      @Param("warehouseId") Long warehouseId,
                                                      @Param("keyword") String keyword);

    @Query("""
            select new com.sme.erp.reports.dto.PurchaseReportRowDTO(
                p.purchaseCode,
                s.name,
                w.name,
                p.purchaseDate,
                p.netTotal,
                p.paidAmount,
                p.dueAmount,
                p.status
            )
            from PurchaseOrder p
            join p.supplier s
            join p.warehouse w
            where (:startDate is null or p.purchaseDate >= :startDate)
              and (:endDate is null or p.purchaseDate < :endDate)
              and (:supplierId is null or s.id = :supplierId)
              and (:warehouseId is null or w.id = :warehouseId)
              and (:status is null or p.status = :status)
              and (:keyword is null or lower(p.purchaseCode) like lower(concat('%', :keyword, '%'))
                   or lower(s.name) like lower(concat('%', :keyword, '%'))
                   or lower(w.name) like lower(concat('%', :keyword, '%')))
            order by p.purchaseDate desc, p.id desc
            """)
    List<PurchaseReportRowDTO> findPurchaseDetailRowsWithStatus(@Param("startDate") LocalDateTime startDate,
                                                                @Param("endDate") LocalDateTime endDate,
                                                                @Param("supplierId") Long supplierId,
                                                                @Param("warehouseId") Long warehouseId,
                                                                @Param("status") PurchaseStatus status,
                                                                @Param("keyword") String keyword);

    @Query("""
            select new com.sme.erp.reports.dto.PurchaseByProductRowDTO(
                product.id,
                product.productName,
                product.sku,
                coalesce(sum(item.quantity), 0),
                coalesce(sum(item.subTotal), 0),
                coalesce(sum(item.returnedQuantity), 0),
                coalesce(sum(item.quantity), 0) - coalesce(sum(item.returnedQuantity), 0)
            )
            from PurchaseOrder p
            join p.items item
            join item.product product
            join p.warehouse w
            left join product.category c
            left join product.brand b
            where (:startDate is null or p.purchaseDate >= :startDate)
              and (:endDate is null or p.purchaseDate < :endDate)
              and (:supplierId is null or p.supplier.id = :supplierId)
              and (:productId is null or product.id = :productId)
              and (:warehouseId is null or w.id = :warehouseId)
              and (:categoryId is null or c.id = :categoryId)
              and (:brandId is null or b.id = :brandId)
              and (:status is null or p.status = :status)
              and (:keyword is null or lower(product.productName) like lower(concat('%', :keyword, '%'))
                   or lower(product.sku) like lower(concat('%', :keyword, '%')))
            group by product.id, product.productName, product.sku
            order by coalesce(sum(item.quantity), 0) desc, coalesce(sum(item.subTotal), 0) desc
            """)
    List<PurchaseByProductRowDTO> findPurchaseByProductRows(@Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate,
                                                            @Param("supplierId") Long supplierId,
                                                            @Param("productId") Long productId,
                                                            @Param("warehouseId") Long warehouseId,
                                                            @Param("categoryId") Long categoryId,
                                                            @Param("brandId") Long brandId,
                                                            @Param("status") PurchaseStatus status,
                                                            @Param("keyword") String keyword);

    @Query("""
            select new com.sme.erp.reports.dto.SupplierPurchaseRowDTO(
                s.id,
                s.name,
                count(p.id),
                coalesce(sum(p.netTotal), 0),
                coalesce(sum(p.paidAmount), 0),
                coalesce(sum(p.dueAmount), 0),
                max(p.purchaseDate)
            )
            from PurchaseOrder p
            join p.supplier s
            where (:startDate is null or p.purchaseDate >= :startDate)
              and (:endDate is null or p.purchaseDate < :endDate)
              and (:supplierId is null or s.id = :supplierId)
              and (:keyword is null or lower(s.name) like lower(concat('%', :keyword, '%'))
                   or lower(p.purchaseCode) like lower(concat('%', :keyword, '%')))
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
            group by s.id, s.name
            order by coalesce(sum(p.netTotal), 0) desc, s.name asc
            """)
    List<SupplierPurchaseRowDTO> findSupplierPurchaseRows(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          @Param("supplierId") Long supplierId,
                                                          @Param("keyword") String keyword);

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
            select p.supplier.id, coalesce(sum(p.dueAmount), 0), coalesce(sum(p.netTotal), 0)
            from PurchaseOrder p
            where (:supplierId is null or p.supplier.id = :supplierId)
              and p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.RECEIVED,
                com.sme.erp.purchase.enums.PurchaseStatus.PARTIAL_PAID,
                com.sme.erp.purchase.enums.PurchaseStatus.PAID
              )
              and (:fromDate is null or p.purchaseDate >= :fromDate)
              and (:toDate is null or p.purchaseDate < :toDate)
            group by p.supplier.id
            """)
    List<Object[]> findApReconciliationPurchaseRows(@Param("supplierId") Long supplierId,
                                                    @Param("fromDate") LocalDateTime fromDate,
                                                    @Param("toDate") LocalDateTime toDate);

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
            where p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.SUBMITTED,
                com.sme.erp.purchase.enums.PurchaseStatus.PENDING
            )
            order by p.purchaseDate desc, p.id desc
            """)
    List<PurchaseOrder> findSubmittedForApproval(Pageable pageable);

    @Query("""
            select count(p)
            from PurchaseOrder p
            where p.status in (
                com.sme.erp.purchase.enums.PurchaseStatus.SUBMITTED,
                com.sme.erp.purchase.enums.PurchaseStatus.PENDING
            )
            """)
    long countSubmittedForApproval();

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
