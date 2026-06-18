package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.AccountDTO;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.service.AccountService;
import com.sme.erp.enums.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting/accounts")
@CrossOrigin("*")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ACCOUNTING_VIEW','ACCOUNTING_CREATE','ACCOUNTING_EDIT','BUDGET_VIEW','BUDGET_CREATE','BUDGET_EDIT')")
    public ResponseEntity<List<AccountDTO>> getAll(@RequestParam(required = false) AccountType type, @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(service.getAll(type, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ACCOUNTING_VIEW','ACCOUNTING_CREATE','ACCOUNTING_EDIT','BUDGET_VIEW','BUDGET_CREATE','BUDGET_EDIT')")
    public ResponseEntity<AccountDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_CREATE')")
    public ResponseEntity<AccountDTO> create(@Valid @RequestBody AccountDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING_EDIT')")
    public ResponseEntity<AccountDTO> update(@PathVariable Long id, @Valid @RequestBody AccountDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}
