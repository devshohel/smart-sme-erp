package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.AccountingBookDTO;

import java.util.List;

public interface AccountingBookService {
    AccountingBookDTO getCashBook(java.time.LocalDate fromDate, java.time.LocalDate toDate);
    AccountingBookDTO getBankBook(java.time.LocalDate fromDate, java.time.LocalDate toDate);
}
