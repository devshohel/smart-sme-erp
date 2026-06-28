package com.sme.erp.sales.service;

import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.dto.SalesReturnContextDTO;

import java.util.List;

public interface SalesReturnService {
    List<SalesReturnDTO> getAll();
    SalesReturnDTO getById(Long id);
    SalesReturnContextDTO getContext(Long invoiceId);
    SalesReturnDTO create(SalesReturnDTO dto);
    SalesReturnDTO update(Long id, SalesReturnDTO dto);
    SalesReturnDTO submit(Long id);
    SalesReturnDTO approve(Long id);
    SalesReturnDTO reject(Long id, String reason);
    SalesReturnDTO post(Long id);
    SalesReturnDTO reverse(Long id, String reversalReason);
    SalesReturnDTO cancel(Long id, String reason);
}
