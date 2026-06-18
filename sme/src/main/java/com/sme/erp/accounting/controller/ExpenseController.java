package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.dto.ExpensePageDTO;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/accounting/expenses", "/api/v1/expenses"})
@CrossOrigin("*")
public class ExpenseController {
    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    public ResponseEntity<List<ExpenseDTO>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) AccountingPaymentMethod paymentMethod) {
        return ResponseEntity.ok(service.getAll(fromDate, toDate, categoryId, paymentMethod));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    public ResponseEntity<ExpensePageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) AccountingPaymentMethod paymentMethod,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.searchPage(keyword, fromDate, toDate, categoryId, paymentMethod, status,
                page, size, sort, direction));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_VIEW')")
    public ResponseEntity<ExpenseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EXPENSE_CREATE')")
    public ResponseEntity<ExpenseDTO> create(@Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_EDIT')")
    public ResponseEntity<ExpenseDTO> update(@PathVariable Long id, @Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_CANCEL')")
    public ResponseEntity<ExpenseDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('EXPENSE_POST')")
    public ResponseEntity<ExpenseDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }
}
