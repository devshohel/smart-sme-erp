package com.sme.erp.customer.receipt.service;

import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.dto.CustomerReceiptPageDTO;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;

import java.time.LocalDate;
import java.util.List;

public interface CustomerReceiptService {
    List<CustomerReceiptDTO> getAll();
    CustomerReceiptPageDTO searchPage(String keyword, Long customerId, CustomerReceiptStatus status,
                                      CustomerReceiptPaymentMethod paymentMethod, LocalDate fromDate, LocalDate toDate,
                                      int page, int size, String sort, String direction);
    CustomerReceiptDTO getById(Long id);
    CustomerReceiptDTO create(CustomerReceiptDTO dto);
    CustomerReceiptDTO update(Long id, CustomerReceiptDTO dto);
    CustomerReceiptDTO post(Long id);
    CustomerReceiptDTO cancel(Long id);
}
