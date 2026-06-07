package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.BookEntryDTO;

import java.util.List;

public interface AccountingBookService {
    List<BookEntryDTO> getCashBook();
    List<BookEntryDTO> getBankBook();
}
