package com.sme.erp.sales.service;

import com.sme.erp.sales.dto.SalesInvoiceDTO;

import java.util.List;

public interface SalesInvoiceService {
    List<SalesInvoiceDTO> getAll();
    SalesInvoiceDTO getById(Long id);
    SalesInvoiceDTO create(SalesInvoiceDTO dto);
    SalesInvoiceDTO update(Long id, SalesInvoiceDTO dto);
}
