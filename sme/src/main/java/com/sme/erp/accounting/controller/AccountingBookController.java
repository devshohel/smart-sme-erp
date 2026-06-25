package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.AccountingBookDTO;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import com.sme.erp.accounting.service.AccountingBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting")
public class AccountingBookController {
    private final AccountingBookService service;

    public AccountingBookController(AccountingBookService service) {
        this.service = service;
    }

    @GetMapping("/cash-book")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<AccountingBookDTO> cashBook(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getCashBook(fromDate, toDate));
    }

    @GetMapping("/bank-book")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<AccountingBookDTO> bankBook(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(service.getBankBook(fromDate, toDate));
    }
}
