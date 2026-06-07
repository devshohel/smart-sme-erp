package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.ExpenseCategoryDTO;
import com.sme.erp.enums.Status;

import java.util.List;

public interface ExpenseCategoryService {
    List<ExpenseCategoryDTO> getAll(Status status);
    ExpenseCategoryDTO getById(Long id);
    ExpenseCategoryDTO create(ExpenseCategoryDTO dto);
    ExpenseCategoryDTO update(Long id, ExpenseCategoryDTO dto);
    void delete(Long id);
}
