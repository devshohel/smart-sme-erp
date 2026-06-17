package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    Optional<SalesInvoice> findTopByOrderByIdDesc();
    boolean existsByInvoiceNo(String invoiceNo);
    boolean existsByInvoiceNoAndIdNot(String invoiceNo, Long id);

    @Query("""
            select new com.sme.erp.reports.dto.SalesReportRowDTO(
                i.invoiceNo,
                c.name,
                i.saleDate,
                coalesce(sum(item.quantity), 0),
                i.netTotal,
                i.paidAmount,
                i.dueAmount
            )
            from SalesInvoice i
            join i.customer c
            left join i.items item
            where (:startDate is null or i.saleDate >= :startDate)
              and (:endDate is null or i.saleDate < :endDate)
              and (:customerId is null or c.id = :customerId)
              and (:productId is null or item.product.id = :productId)
              and i.status in (com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED, com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED)
            group by i.id, i.invoiceNo, c.name, i.saleDate, i.netTotal, i.paidAmount, i.dueAmount
            order by i.saleDate desc, i.id desc
            """)
    List<SalesReportRowDTO> findSalesReportRows(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("customerId") Long customerId,
            @Param("productId") Long productId);

    @Query("""
            select new com.sme.erp.reports.dto.CustomerDueReportRowDTO(
                c.name,
                coalesce(sum(i.netTotal), 0),
                coalesce(sum(i.paidAmount), 0),
                coalesce(sum(i.dueAmount), 0)
            )
            from SalesInvoice i
            join i.customer c
            where i.status in (com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED, com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED)
            group by c.id, c.name
            having coalesce(sum(i.dueAmount), 0) > 0
            order by coalesce(sum(i.dueAmount), 0) desc
            """)
    List<CustomerDueReportRowDTO> findCustomerDueReportRows();

    long countByCustomerId(Long customerId);

    @Query("select coalesce(sum(i.dueAmount), 0) from SalesInvoice i where i.customer.id = :customerId")
    java.math.BigDecimal sumDueByCustomerId(@Param("customerId") Long customerId);

    @Query("select max(i.saleDate) from SalesInvoice i where i.customer.id = :customerId")
    LocalDateTime findLastInvoiceDateByCustomerId(@Param("customerId") Long customerId);

    @Query("select max(i.saleDate) from SalesInvoice i where i.customer.id = :customerId and i.paidAmount > 0")
    LocalDateTime findLastPaymentDateByCustomerId(@Param("customerId") Long customerId);

    List<SalesInvoice> findByCustomerIdOrderBySaleDateDescIdDesc(Long customerId, Pageable pageable);
}
