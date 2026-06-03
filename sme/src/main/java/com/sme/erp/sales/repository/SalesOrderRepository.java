package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findTopByOrderByIdDesc();
    boolean existsByOrderNo(String orderNo);
    boolean existsByOrderNoAndIdNot(String orderNo, Long id);
}
