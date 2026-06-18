package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.ExpenseCategoryDTO;
import com.sme.erp.accounting.service.ExpenseCategoryService;
import com.sme.erp.enums.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting/expense-categories")
@CrossOrigin("*")
public class ExpenseCategoryController {
    private final ExpenseCategoryService service;

    public ExpenseCategoryController(ExpenseCategoryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EXPENSE_VIEW','EXPENSE_CREATE','EXPENSE_EDIT')")
    public ResponseEntity<List<ExpenseCategoryDTO>> getAll(@RequestParam(required = false) Status status) {
        return ResponseEntity.ok(service.getAll(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    public ResponseEntity<ExpenseCategoryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_CREATE')")
    public ResponseEntity<ExpenseCategoryDTO> create(@Valid @RequestBody ExpenseCategoryDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING_EDIT')")
    public ResponseEntity<ExpenseCategoryDTO> update(@PathVariable Long id, @Valid @RequestBody ExpenseCategoryDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
