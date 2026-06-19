package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.reports.dto.CustomerSalesRowDTO;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.TopSellingProductRowDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    Optional<SalesInvoice> findTopByOrderByIdDesc();
    Optional<SalesInvoice> findFirstByOrderId(Long orderId);
    boolean existsByInvoiceNo(String invoiceNo);
    boolean existsByInvoiceNoAndIdNot(String invoiceNo, Long id);

    @Query("""
            select new com.sme.erp.reports.dto.SalesReportRowDTO(
                i.invoiceNo,
                c.name,
                w.name,
                i.status,
                i.saleDate,
                coalesce(sum(item.quantity), 0),
                i.netTotal,
                i.paidAmount,
                i.dueAmount
            )
            from SalesInvoice i
            join i.customer c
            join i.warehouse w
            left join i.items item
            where (:startDate is null or i.saleDate >= :startDate)
              and (:endDate is null or i.saleDate < :endDate)
              and (:customerId is null or c.id = :customerId)
              and (:productId is null or item.product.id = :productId)
            and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
            )
            group by i.id, i.invoiceNo, c.name, w.name, i.status, i.saleDate, i.netTotal, i.paidAmount, i.dueAmount
            order by i.saleDate desc, i.id desc
            """)
    List<SalesReportRowDTO> findSalesReportRows(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("customerId") Long customerId,
            @Param("productId") Long productId);

    @Query("""
            select new com.sme.erp.reports.dto.SalesReportRowDTO(
                i.invoiceNo,
                c.name,
                w.name,
                i.status,
                i.saleDate,
                coalesce(sum(item.quantity), 0),
                i.netTotal,
                i.paidAmount,
                i.dueAmount
            )
            from SalesInvoice i
            join i.customer c
            join i.warehouse w
            left join i.items item
            where (:startDate is null or i.saleDate >= :startDate)
              and (:endDate is null or i.saleDate < :endDate)
              and (:customerId is null or c.id = :customerId)
              and (:productId is null or item.product.id = :productId)
              and (:warehouseId is null or w.id = :warehouseId)
              and (:keyword is null or lower(i.invoiceNo) like lower(concat('%', :keyword, '%'))
                   or lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(w.name) like lower(concat('%', :keyword, '%')))
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
            group by i.id, i.invoiceNo, c.name, w.name, i.status, i.saleDate, i.netTotal, i.paidAmount, i.dueAmount
            order by i.saleDate desc, i.id desc
            """)
    List<SalesReportRowDTO> findSalesDetailRows(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                @Param("customerId") Long customerId,
                                                @Param("productId") Long productId,
                                                @Param("warehouseId") Long warehouseId,
                                                @Param("keyword") String keyword);

    @Query("""
            select new com.sme.erp.reports.dto.TopSellingProductRowDTO(
                p.id,
                p.productName,
                p.sku,
                coalesce(sum(item.quantity), 0),
                coalesce(sum(item.subTotal), 0),
                0,
                coalesce(sum(item.quantity), 0)
            )
            from SalesInvoice i
            join i.items item
            join item.product p
            join i.warehouse w
            left join p.category c
            left join p.brand b
            where (:startDate is null or i.saleDate >= :startDate)
              and (:endDate is null or i.saleDate < :endDate)
              and (:productId is null or p.id = :productId)
              and (:warehouseId is null or w.id = :warehouseId)
              and (:categoryId is null or c.id = :categoryId)
              and (:brandId is null or b.id = :brandId)
              and (:keyword is null or lower(p.productName) like lower(concat('%', :keyword, '%'))
                   or lower(p.sku) like lower(concat('%', :keyword, '%')))
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
            group by p.id, p.productName, p.sku
            order by coalesce(sum(item.quantity), 0) desc, coalesce(sum(item.subTotal), 0) desc
            """)
    List<TopSellingProductRowDTO> findTopSellingProductRows(@Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate,
                                                            @Param("productId") Long productId,
                                                            @Param("warehouseId") Long warehouseId,
                                                            @Param("categoryId") Long categoryId,
                                                            @Param("brandId") Long brandId,
                                                            @Param("keyword") String keyword);

    @Query("""
            select new com.sme.erp.reports.dto.CustomerSalesRowDTO(
                c.id,
                c.name,
                count(i.id),
                coalesce(sum(i.netTotal), 0),
                coalesce(sum(i.paidAmount), 0),
                coalesce(sum(i.dueAmount), 0),
                max(i.saleDate)
            )
            from SalesInvoice i
            join i.customer c
            where (:startDate is null or i.saleDate >= :startDate)
              and (:endDate is null or i.saleDate < :endDate)
              and (:customerId is null or c.id = :customerId)
              and (:keyword is null or lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(i.invoiceNo) like lower(concat('%', :keyword, '%')))
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
            group by c.id, c.name
            order by coalesce(sum(i.netTotal), 0) desc, c.name asc
            """)
    List<CustomerSalesRowDTO> findCustomerSalesRows(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    @Param("customerId") Long customerId,
                                                    @Param("keyword") String keyword);

    @Query("""
            select new com.sme.erp.reports.dto.CustomerDueReportRowDTO(
                c.name,
                coalesce(sum(i.netTotal), 0),
                coalesce(sum(i.paidAmount), 0),
                coalesce(sum(i.dueAmount), 0)
            )
            from SalesInvoice i
            join i.customer c
            where i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
            )
            group by c.id, c.name
            having coalesce(sum(i.dueAmount), 0) > 0
            order by coalesce(sum(i.dueAmount), 0) desc
            """)
    List<CustomerDueReportRowDTO> findCustomerDueReportRows();

    long countByCustomerId(Long customerId);

    @Query("""
            select coalesce(sum(i.dueAmount), 0)
            from SalesInvoice i
            where i.customer.id = :customerId
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
            """)
    java.math.BigDecimal sumDueByCustomerId(@Param("customerId") Long customerId);

    @Query("""
            select i.customer.id, coalesce(sum(i.dueAmount), 0)
            from SalesInvoice i
            where i.customer.id in :customerIds
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
            group by i.customer.id
            """)
    List<Object[]> sumDueByCustomerIds(@Param("customerIds") List<Long> customerIds);

    @Query("""
            select i
            from SalesInvoice i
            where i.customer.id = :customerId
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
              and i.dueAmount > 0
            order by i.saleDate asc, i.id asc
            """)
    List<SalesInvoice> findUnpaidByCustomerIdOrderBySaleDateAscIdAsc(@Param("customerId") Long customerId);

    @Query("select max(i.saleDate) from SalesInvoice i where i.customer.id = :customerId")
    LocalDateTime findLastInvoiceDateByCustomerId(@Param("customerId") Long customerId);

    @Query("select max(i.saleDate) from SalesInvoice i where i.customer.id = :customerId and i.paidAmount > 0")
    LocalDateTime findLastPaymentDateByCustomerId(@Param("customerId") Long customerId);

    List<SalesInvoice> findByCustomerIdOrderBySaleDateDescIdDesc(Long customerId, Pageable pageable);

    @Query("""
            select i
            from SalesInvoice i
            where i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.SUBMITTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PENDING
            )
            order by i.saleDate desc, i.id desc
            """)
    List<SalesInvoice> findSubmittedForApproval(Pageable pageable);

    @Query("""
            select count(i)
            from SalesInvoice i
            where i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.SUBMITTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PENDING
            )
            """)
    long countSubmittedForApproval();

    @Query("""
            select i
            from SalesInvoice i
            join fetch i.customer c
            where c.id = :customerId
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
              and (:fromDate is null or i.saleDate >= :fromDate)
              and (:toDate is null or i.saleDate < :toDate)
            order by i.saleDate asc, i.id asc
            """)
    List<SalesInvoice> findPostedByCustomerForLedger(@Param("customerId") Long customerId,
                                                     @Param("fromDate") LocalDateTime fromDate,
                                                     @Param("toDate") LocalDateTime toDate);

    @Query("""
            select i
            from SalesInvoice i
            join fetch i.customer c
            where (:customerId is null or c.id = :customerId)
              and i.status in (
                com.sme.erp.sales.enums.SalesInvoiceStatus.POSTED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PARTIAL_PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.PAID,
                com.sme.erp.sales.enums.SalesInvoiceStatus.CONFIRMED,
                com.sme.erp.sales.enums.SalesInvoiceStatus.COMPLETED
              )
              and i.dueAmount > 0
              and (:fromDate is null or i.saleDate >= :fromDate)
              and (:toDate is null or i.saleDate < :toDate)
            order by c.name asc, i.saleDate asc, i.id asc
            """)
    List<SalesInvoice> findDueInvoicesForAging(@Param("customerId") Long customerId,
                                               @Param("fromDate") LocalDateTime fromDate,
                                               @Param("toDate") LocalDateTime toDate);
}
