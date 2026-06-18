package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.BalanceSheetDTO;
import com.sme.erp.accounting.dto.LedgerEntryDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;
import com.sme.erp.accounting.dto.GeneralLedgerDTO;
import com.sme.erp.accounting.dto.AccountLedgerDTO;
import com.sme.erp.accounting.dto.ProfitLossDTO;

import java.time.LocalDate;
import java.util.List;

public interface AccountingReportService {
    List<LedgerEntryDTO> getCustomerLedger(Long customerId, LocalDate fromDate, LocalDate toDate);
    List<LedgerEntryDTO> getSupplierLedger(Long supplierId, LocalDate fromDate, LocalDate toDate);
    GeneralLedgerDTO getGeneralLedger(LocalDate fromDate, LocalDate toDate);
    AccountLedgerDTO getAccountLedger(Long accountId, LocalDate fromDate, LocalDate toDate);
    ProfitLossDTO getProfitLoss(LocalDate fromDate, LocalDate toDate);
    TrialBalanceDTO getTrialBalance(LocalDate fromDate, LocalDate toDate);
    BalanceSheetDTO getBalanceSheet(LocalDate asOfDate);
}
