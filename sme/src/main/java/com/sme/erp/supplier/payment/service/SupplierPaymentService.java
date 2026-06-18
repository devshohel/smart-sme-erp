package com.sme.erp.supplier.payment.service;

import com.sme.erp.supplier.payment.dto.SupplierPaymentDTO;
import com.sme.erp.supplier.payment.dto.SupplierPaymentPageDTO;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;

import java.time.LocalDate;
import java.util.List;

public interface SupplierPaymentService {
    List<SupplierPaymentDTO> getAll();
    SupplierPaymentPageDTO searchPage(String keyword, Long supplierId, SupplierPaymentStatus status,
                                      SupplierPaymentMethod paymentMethod, LocalDate fromDate, LocalDate toDate,
                                      int page, int size, String sort, String direction);
    SupplierPaymentDTO getById(Long id);
    SupplierPaymentDTO create(SupplierPaymentDTO dto);
    SupplierPaymentDTO update(Long id, SupplierPaymentDTO dto);
    SupplierPaymentDTO post(Long id);
    SupplierPaymentDTO cancel(Long id);
    SupplierPaymentDTO reverse(Long id, String reversalReason);
}
