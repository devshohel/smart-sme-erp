package com.sme.erp.sales.service;

import com.sme.erp.sales.dto.SalesOrderDTO;

import java.util.List;

public interface SalesOrderService {
    List<SalesOrderDTO> getAll();
    SalesOrderDTO getById(Long id);
    SalesOrderDTO create(SalesOrderDTO dto);
    SalesOrderDTO update(Long id, SalesOrderDTO dto);
}
