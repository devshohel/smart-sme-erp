package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesReturn;
import com.sme.erp.reports.dto.SalesReturnRowDTO;
import com.sme.erp.sales.enums.SalesReturnStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {
    Optional<SalesReturn> findTopByOrderByIdDesc();
    boolean existsByReturnCode(String returnCode);
    boolean existsByReturnCodeAndIdNot(String returnCode, Long id);
    boolean existsByInvoiceIdAndStatus(Long invoiceId, com.sme.erp.sales.enums.SalesReturnStatus status);
    List<SalesReturn> findByCustomerIdOrderByReturnDateDescIdDesc(Long customerId, Pageable pageable);

    @Query("""
            select distinct r
            from SalesReturn r
            join fetch r.items item
            left join fetch item.product
            where r.invoice.id = :invoiceId
              and r.status = com.sme.erp.sales.enums.SalesReturnStatus.POSTED
              and (:excludeId is null or r.id <> :excludeId)
            """)
    List<SalesReturn> findPostedByInvoiceIdExcluding(@Param("invoiceId") Long invoiceId,
                                                     @Param("excludeId") Long excludeId);

    @Query("""
            select r
            from SalesReturn r
            where r.customer.id = :customerId
              and (:fromDate is null or r.returnDate >= :fromDate)
              and (:toDate is null or r.returnDate < :toDate)
            order by r.returnDate asc, r.id asc
            """)
    List<SalesReturn> findByCustomerForLedger(@Param("customerId") Long customerId,
                                              @Param("fromDate") LocalDateTime fromDate,
                                              @Param("toDate") LocalDateTime toDate);

    @Query("""
            select coalesce(sum(r.totalAmount), 0)
            from SalesReturn r
            where (:startDate is null or r.returnDate >= :startDate)
              and (:endDate is null or r.returnDate < :endDate)
              and (:customerId is null or r.customer.id = :customerId)
            """)
    java.math.BigDecimal sumReturnAmount(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("customerId") Long customerId);

    @Query("""
            select new com.sme.erp.reports.dto.SalesReturnRowDTO(
                r.returnCode,
                c.name,
                i.invoiceNo,
                r.returnDate,
                coalesce(sum(item.quantity), 0),
                r.totalAmount,
                r.status
            )
            from SalesReturn r
            join r.customer c
            join r.invoice i
            left join r.items item
            left join item.product p
            left join i.warehouse w
            where (:startDate is null or r.returnDate >= :startDate)
              and (:endDate is null or r.returnDate < :endDate)
              and (:customerId is null or c.id = :customerId)
              and (:productId is null or p.id = :productId)
              and (:warehouseId is null or w.id = :warehouseId)
              and (:status is null or r.status = :status)
              and (:keyword is null or lower(r.returnCode) like lower(concat('%', :keyword, '%'))
                   or lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(i.invoiceNo) like lower(concat('%', :keyword, '%')))
            group by r.id, r.returnCode, c.name, i.invoiceNo, r.returnDate, r.totalAmount, r.status
            order by r.returnDate desc, r.id desc
            """)
    List<SalesReturnRowDTO> findSalesReturnReportRows(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      @Param("customerId") Long customerId,
                                                      @Param("productId") Long productId,
                                                      @Param("warehouseId") Long warehouseId,
                                                      @Param("status") SalesReturnStatus status,
                                                      @Param("keyword") String keyword);
}
