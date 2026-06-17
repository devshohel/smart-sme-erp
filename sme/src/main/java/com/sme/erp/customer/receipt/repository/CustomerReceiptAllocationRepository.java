package com.sme.erp.customer.receipt.repository;

import com.sme.erp.customer.receipt.entity.CustomerReceiptAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerReceiptAllocationRepository extends JpaRepository<CustomerReceiptAllocation, Long> {
    List<CustomerReceiptAllocation> findByReceiptIdOrderByIdAsc(Long receiptId);
}
