package com.sme.erp.sales.service;

import com.sme.erp.sales.dto.SalesInvoiceDTO;

import java.util.List;

public interface SalesInvoiceService {
    List<SalesInvoiceDTO> getAll();
    List<SalesInvoiceDTO> getUnpaidByCustomerId(Long customerId);
    SalesInvoiceDTO getById(Long id);
    SalesInvoiceDTO create(SalesInvoiceDTO dto);
    SalesInvoiceDTO update(Long id, SalesInvoiceDTO dto);
    SalesInvoiceDTO submit(Long id);
    SalesInvoiceDTO approve(Long id);
    SalesInvoiceDTO post(Long id);
    SalesInvoiceDTO cancel(Long id);
    SalesInvoiceDTO reverse(Long id, String reversalReason);
    void deleteDraft(Long id);
}
