package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.BalanceSheetDTO;
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
@CrossOrigin("*")
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
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<List<LedgerEntryDTO>> generalLedger(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getGeneralLedger(accountId, fromDate, toDate));
    }

    @GetMapping("/trial-balance")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<TrialBalanceDTO> trialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getTrialBalance(fromDate, toDate));
    }

    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<BalanceSheetDTO> balanceSheet() {
        return ResponseEntity.ok(service.getBalanceSheet());
    }
}
