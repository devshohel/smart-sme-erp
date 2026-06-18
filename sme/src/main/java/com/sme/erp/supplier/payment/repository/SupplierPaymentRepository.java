package com.sme.erp.supplier.payment.repository;

import com.sme.erp.supplier.payment.entity.SupplierPayment;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
    Optional<SupplierPayment> findTopByOrderByIdDesc();
    boolean existsByPaymentNo(String paymentNo);

    @EntityGraph(attributePaths = {"supplier", "allocations", "allocations.purchaseOrder"})
    Optional<SupplierPayment> findDetailedById(Long id);

    List<SupplierPayment> findBySupplierIdOrderByPaymentDateDescIdDesc(Long supplierId, Pageable pageable);

    @Query("""
            select p
            from SupplierPayment p
            where p.supplier.id = :supplierId
              and p.status = com.sme.erp.supplier.payment.enums.SupplierPaymentStatus.POSTED
              and (:fromDate is null or p.paymentDate >= :fromDate)
              and (:toDate is null or p.paymentDate <= :toDate)
            order by p.paymentDate asc, p.id asc
            """)
    List<SupplierPayment> findPostedBySupplierForLedger(@Param("supplierId") Long supplierId,
                                                        @Param("fromDate") LocalDate fromDate,
                                                        @Param("toDate") LocalDate toDate);

    @EntityGraph(attributePaths = {"supplier", "allocations", "allocations.purchaseOrder"})
    @Query("""
            select p
            from SupplierPayment p
            where p.supplier.id = :supplierId
              and p.status in (
                com.sme.erp.supplier.payment.enums.SupplierPaymentStatus.POSTED,
                com.sme.erp.supplier.payment.enums.SupplierPaymentStatus.REVERSED
              )
            order by p.paymentDate asc, p.id asc
            """)
    List<SupplierPayment> findStatementPaymentsBySupplier(@Param("supplierId") Long supplierId);

    @Query("""
            select p.supplier.id, coalesce(sum(p.unappliedAmount), 0)
            from SupplierPayment p
            where (:supplierId is null or p.supplier.id = :supplierId)
              and p.status = com.sme.erp.supplier.payment.enums.SupplierPaymentStatus.POSTED
              and (:fromDate is null or p.paymentDate >= :fromDate)
              and (:toDate is null or p.paymentDate <= :toDate)
            group by p.supplier.id
            """)
    List<Object[]> findApReconciliationAdvanceRows(@Param("supplierId") Long supplierId,
                                                   @Param("fromDate") LocalDate fromDate,
                                                   @Param("toDate") LocalDate toDate);

    @Query(value = """
            select p
            from SupplierPayment p
            join p.supplier s
            where (:supplierId is null or s.id = :supplierId)
              and (:status is null or p.status = :status)
              and (:paymentMethod is null or p.paymentMethod = :paymentMethod)
              and (:fromDate is null or p.paymentDate >= :fromDate)
              and (:toDate is null or p.paymentDate <= :toDate)
              and (
                    :keyword is null
                    or lower(coalesce(p.paymentNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(s.supplierCode, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(s.name, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.referenceNo, '')) like lower(concat('%', :keyword, '%'))
              )
            """,
            countQuery = """
            select count(p)
            from SupplierPayment p
            join p.supplier s
            where (:supplierId is null or s.id = :supplierId)
              and (:status is null or p.status = :status)
              and (:paymentMethod is null or p.paymentMethod = :paymentMethod)
              and (:fromDate is null or p.paymentDate >= :fromDate)
              and (:toDate is null or p.paymentDate <= :toDate)
              and (
                    :keyword is null
                    or lower(coalesce(p.paymentNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(s.supplierCode, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(s.name, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(p.referenceNo, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<SupplierPayment> searchPage(@Param("keyword") String keyword,
                                     @Param("supplierId") Long supplierId,
                                     @Param("status") SupplierPaymentStatus status,
                                     @Param("paymentMethod") SupplierPaymentMethod paymentMethod,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     Pageable pageable);
}
