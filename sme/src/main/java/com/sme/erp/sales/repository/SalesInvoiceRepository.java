package com.sme.erp.sales.repository;

import com.sme.erp.sales.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
    Optional<SalesInvoice> findTopByOrderByIdDesc();
    boolean existsByInvoiceNo(String invoiceNo);
    boolean existsByInvoiceNoAndIdNot(String invoiceNo, Long id);
}
