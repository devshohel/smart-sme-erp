package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.BookEntryDTO;
import com.sme.erp.accounting.service.AccountingBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting")
@CrossOrigin("*")
public class AccountingBookController {
    private final AccountingBookService service;

    public AccountingBookController(AccountingBookService service) {
        this.service = service;
    }

    @GetMapping("/cash-book")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<List<BookEntryDTO>> cashBook() {
        return ResponseEntity.ok(service.getCashBook());
    }

    @GetMapping("/bank-book")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<List<BookEntryDTO>> bankBook() {
        return ResponseEntity.ok(service.getBankBook());
    }
}
