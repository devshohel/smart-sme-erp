package com.sme.erp.sales.service;

import com.sme.erp.sales.dto.SalesReturnDTO;

import java.util.List;

public interface SalesReturnService {
    List<SalesReturnDTO> getAll();
    SalesReturnDTO getById(Long id);
    SalesReturnDTO create(SalesReturnDTO dto);
}
