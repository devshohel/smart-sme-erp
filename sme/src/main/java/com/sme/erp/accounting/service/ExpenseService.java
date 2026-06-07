package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    List<ExpenseDTO> getAll(LocalDate fromDate, LocalDate toDate, Long categoryId, AccountingPaymentMethod paymentMethod);
    ExpenseDTO getById(Long id);
    ExpenseDTO create(ExpenseDTO dto);
    ExpenseDTO update(Long id, ExpenseDTO dto);
    ExpenseDTO cancel(Long id);
}
