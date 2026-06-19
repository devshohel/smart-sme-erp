package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.PurchaseReturn;
import com.sme.erp.reports.dto.PurchaseReturnRowDTO;
import com.sme.erp.supplier.dto.SupplierReturnSummaryDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {
    Optional<PurchaseReturn> findTopByOrderByIdDesc();
    boolean existsByReturnCode(String returnCode);
    boolean existsByReturnCodeAndIdNot(String returnCode, Long id);

    @Query("""
            select new com.sme.erp.supplier.dto.SupplierReturnSummaryDTO(
                r.returnCode,
                r.returnDate,
                r.totalAmount,
                'RETURNED'
            )
            from PurchaseReturn r
            where r.supplier.id = :supplierId
            order by r.returnDate desc, r.id desc
            """)
    List<SupplierReturnSummaryDTO> findRecentReturnSummariesBySupplierId(@Param("supplierId") Long supplierId, Pageable pageable);

    @Query("""
            select r
            from PurchaseReturn r
            where r.supplier.id = :supplierId
              and (:fromDate is null or r.returnDate >= :fromDate)
              and (:toDate is null or r.returnDate < :toDate)
            order by r.returnDate asc, r.id asc
            """)
    List<PurchaseReturn> findBySupplierForLedger(@Param("supplierId") Long supplierId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate);

    @Query("""
            select r.supplier.id, coalesce(sum(r.totalAmount), 0)
            from PurchaseReturn r
            where (:supplierId is null or r.supplier.id = :supplierId)
              and (:fromDate is null or r.returnDate >= :fromDate)
              and (:toDate is null or r.returnDate < :toDate)
            group by r.supplier.id
            """)
    List<Object[]> findApReconciliationReturnRows(@Param("supplierId") Long supplierId,
                                                  @Param("fromDate") LocalDateTime fromDate,
                                                  @Param("toDate") LocalDateTime toDate);

    @Query("""
            select new com.sme.erp.reports.dto.PurchaseReturnRowDTO(
                r.returnCode,
                s.name,
                p.purchaseCode,
                r.returnDate,
                r.totalAmount,
                'RETURNED'
            )
            from PurchaseReturn r
            join r.supplier s
            join r.purchase p
            where (:startDate is null or r.returnDate >= :startDate)
              and (:endDate is null or r.returnDate < :endDate)
              and (:supplierId is null or s.id = :supplierId)
              and (:keyword is null or lower(r.returnCode) like lower(concat('%', :keyword, '%'))
                   or lower(s.name) like lower(concat('%', :keyword, '%'))
                   or lower(p.purchaseCode) like lower(concat('%', :keyword, '%')))
            order by r.returnDate desc, r.id desc
            """)
    List<PurchaseReturnRowDTO> findPurchaseReturnRows(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      @Param("supplierId") Long supplierId,
                                                      @Param("keyword") String keyword);

    @Query("""
            select coalesce(sum(r.totalAmount), 0)
            from PurchaseReturn r
            where (:startDate is null or r.returnDate >= :startDate)
              and (:endDate is null or r.returnDate < :endDate)
              and (:supplierId is null or r.supplier.id = :supplierId)
            """)
    java.math.BigDecimal sumReturnAmount(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("supplierId") Long supplierId);
}
