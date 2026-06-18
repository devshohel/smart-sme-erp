package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.dto.ExpensePageDTO;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    List<ExpenseDTO> getAll(LocalDate fromDate, LocalDate toDate, Long categoryId, AccountingPaymentMethod paymentMethod);
    ExpensePageDTO searchPage(String keyword, LocalDate fromDate, LocalDate toDate, Long categoryId,
                              AccountingPaymentMethod paymentMethod, ExpenseStatus status,
                              int page, int size, String sort, String direction);
    ExpenseDTO getById(Long id);
    ExpenseDTO create(ExpenseDTO dto);
    ExpenseDTO update(Long id, ExpenseDTO dto);
    ExpenseDTO cancel(Long id);
    ExpenseDTO post(Long id);
}
