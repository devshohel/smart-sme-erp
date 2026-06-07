package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.BalanceSheetDTO;
import com.sme.erp.accounting.dto.LedgerEntryDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;

import java.time.LocalDate;
import java.util.List;

public interface AccountingReportService {
    List<LedgerEntryDTO> getCustomerLedger(Long customerId, LocalDate fromDate, LocalDate toDate);
    List<LedgerEntryDTO> getSupplierLedger(Long supplierId, LocalDate fromDate, LocalDate toDate);
    List<LedgerEntryDTO> getGeneralLedger(Long accountId, LocalDate fromDate, LocalDate toDate);
    TrialBalanceDTO getTrialBalance(LocalDate fromDate, LocalDate toDate);
    BalanceSheetDTO getBalanceSheet();
}
