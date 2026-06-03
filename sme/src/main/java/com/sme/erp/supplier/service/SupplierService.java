package com.sme.erp.supplier.service;

import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.SupplierDTO;

import java.util.List;

public interface SupplierService {
    List<SupplierDTO> getAll(String keyword, Status status);
    SupplierDTO getById(Long id);
    SupplierDTO create(SupplierDTO dto);
    SupplierDTO update(Long id, SupplierDTO dto);
    void delete(Long id);
}
