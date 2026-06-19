package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesReturn;
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
}
