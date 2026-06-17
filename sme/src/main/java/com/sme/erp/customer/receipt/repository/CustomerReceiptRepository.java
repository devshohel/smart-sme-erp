package com.sme.erp.customer.receipt.repository;

import com.sme.erp.customer.receipt.entity.CustomerReceipt;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerReceiptRepository extends JpaRepository<CustomerReceipt, Long> {
    Optional<CustomerReceipt> findTopByOrderByIdDesc();
    @EntityGraph(attributePaths = {"customer", "allocations", "allocations.salesInvoice"})
    Optional<CustomerReceipt> findDetailedById(Long id);
    boolean existsByReceiptNo(String receiptNo);
    boolean existsByReceiptNoAndIdNot(String receiptNo, Long id);

    List<CustomerReceipt> findByCustomerIdOrderByReceiptDateDescIdDesc(Long customerId, Pageable pageable);

    @Query(value = """
            select r
            from CustomerReceipt r
            join r.customer c
            where (:customerId is null or c.id = :customerId)
              and (:status is null or r.status = :status)
              and (:paymentMethod is null or r.paymentMethod = :paymentMethod)
              and (:fromDate is null or r.receiptDate >= :fromDate)
              and (:toDate is null or r.receiptDate <= :toDate)
              and (
                    :keyword is null
                    or lower(coalesce(r.receiptNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.customerCode, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.referenceNo, '')) like lower(concat('%', :keyword, '%'))
              )
            """,
            countQuery = """
            select count(r)
            from CustomerReceipt r
            join r.customer c
            where (:customerId is null or c.id = :customerId)
              and (:status is null or r.status = :status)
              and (:paymentMethod is null or r.paymentMethod = :paymentMethod)
              and (:fromDate is null or r.receiptDate >= :fromDate)
              and (:toDate is null or r.receiptDate <= :toDate)
              and (
                    :keyword is null
                    or lower(coalesce(r.receiptNo, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.customerCode, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(c.name, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(r.referenceNo, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<CustomerReceipt> searchPage(@Param("keyword") String keyword,
                                     @Param("customerId") Long customerId,
                                     @Param("status") CustomerReceiptStatus status,
                                     @Param("paymentMethod") CustomerReceiptPaymentMethod paymentMethod,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     Pageable pageable);
}
