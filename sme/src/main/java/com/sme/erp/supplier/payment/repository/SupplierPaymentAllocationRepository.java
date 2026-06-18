package com.sme.erp.supplier.payment.repository;

import com.sme.erp.supplier.payment.entity.SupplierPaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPaymentAllocationRepository extends JpaRepository<SupplierPaymentAllocation, Long> {
    List<SupplierPaymentAllocation> findBySupplierPaymentIdOrderByIdAsc(Long supplierPaymentId);
}
