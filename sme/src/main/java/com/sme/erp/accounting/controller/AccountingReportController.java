package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.*;
import com.sme.erp.accounting.dto.LedgerEntryDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;
import com.sme.erp.accounting.service.AccountingReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting")
public class AccountingReportController {
    private final AccountingReportService service;

    public AccountingReportController(AccountingReportService service) {
        this.service = service;
    }

    @GetMapping("/customer-ledger")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<List<LedgerEntryDTO>> customerLedger(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getCustomerLedger(customerId, fromDate, toDate));
    }

    @GetMapping("/supplier-ledger")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<List<LedgerEntryDTO>> supplierLedger(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getSupplierLedger(supplierId, fromDate, toDate));
    }

    @GetMapping("/general-ledger")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<GeneralLedgerDTO> generalLedger(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getGeneralLedger(fromDate, toDate));
    }

    @GetMapping("/account-ledger/{accountId}")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<AccountLedgerDTO> accountLedger(
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getAccountLedger(accountId, fromDate, toDate));
    }

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ProfitLossDTO> profitLoss(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getProfitLoss(fromDate, toDate));
    }

    @GetMapping("/trial-balance")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<TrialBalanceDTO> trialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getTrialBalance(asOfDate != null ? null : fromDate, asOfDate != null ? asOfDate : toDate));
    }

    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW') or hasAuthority('REPORT_VIEW')")
    public ResponseEntity<BalanceSheetDTO> balanceSheet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ResponseEntity.ok(service.getBalanceSheet(asOfDate));
    }
}
