package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.dto.ExpensePageDTO;
import com.sme.erp.accounting.dto.ExpenseRejectRequestDTO;
import com.sme.erp.accounting.dto.ExpenseReportRowDTO;
import com.sme.erp.accounting.dto.ExpenseReverseRequestDTO;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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

    @GetMapping("/approval-queue")
    @PreAuthorize("hasAuthority('EXPENSE_APPROVE')")
    public ResponseEntity<List<ExpenseDTO>> approvalQueue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String submittedBy,
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax) {
        return ResponseEntity.ok(service.approvalQueue(fromDate, toDate, categoryId, submittedBy, amountMin, amountMax));
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('EXPENSE_CREATE')")
    public ResponseEntity<ExpenseDTO> create(@Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EXPENSE_CREATE')")
    public ResponseEntity<ExpenseDTO> createWithReceipt(
            @Valid @RequestPart("expense") ExpenseDTO dto,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt) {
        return ResponseEntity.ok(service.create(dto, receipt));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('EXPENSE_EDIT')")
    public ResponseEntity<ExpenseDTO> update(@PathVariable Long id, @Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('EXPENSE_EDIT')")
    public ResponseEntity<ExpenseDTO> updateWithReceipt(
            @PathVariable Long id,
            @Valid @RequestPart("expense") ExpenseDTO dto,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt) {
        return ResponseEntity.ok(service.update(id, dto, receipt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EXPENSE_CANCEL')")
    public ResponseEntity<ExpenseDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('EXPENSE_SUBMIT')")
    public ResponseEntity<ExpenseDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('EXPENSE_APPROVE')")
    public ResponseEntity<ExpenseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('EXPENSE_REJECT')")
    public ResponseEntity<ExpenseDTO> reject(@PathVariable Long id, @Valid @RequestBody ExpenseRejectRequestDTO request) {
        return ResponseEntity.ok(service.reject(id, request.getReason()));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('EXPENSE_POST')")
    public ResponseEntity<ExpenseDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority('EXPENSE_REVERSE')")
    public ResponseEntity<ExpenseDTO> reverse(@PathVariable Long id, @Valid @RequestBody ExpenseReverseRequestDTO request) {
        return ResponseEntity.ok(service.reverse(id, request.getReversalReason()));
    }

    @GetMapping("/reports/summary")
    @PreAuthorize("hasAuthority('EXPENSE_REPORT_VIEW')")
    public ResponseEntity<List<ExpenseReportRowDTO>> reportSummary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                   @RequestParam(required = false) Long categoryId,
                                                                   @RequestParam(required = false) ExpenseStatus status) {
        return ResponseEntity.ok(service.reportSummary(fromDate, toDate, categoryId, status));
    }

    @GetMapping("/reports/category")
    @PreAuthorize("hasAuthority('EXPENSE_REPORT_VIEW')")
    public ResponseEntity<List<ExpenseReportRowDTO>> reportCategory(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                    @RequestParam(required = false) Long categoryId,
                                                                    @RequestParam(required = false) ExpenseStatus status) {
        return ResponseEntity.ok(service.reportByCategory(fromDate, toDate, categoryId, status));
    }

    @GetMapping("/reports/payment-method")
    @PreAuthorize("hasAuthority('EXPENSE_REPORT_VIEW')")
    public ResponseEntity<List<ExpenseReportRowDTO>> reportPaymentMethod(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                         @RequestParam(required = false) Long categoryId,
                                                                         @RequestParam(required = false) ExpenseStatus status) {
        return ResponseEntity.ok(service.reportByPaymentMethod(fromDate, toDate, categoryId, status));
    }

    @GetMapping("/reports/tax")
    @PreAuthorize("hasAuthority('EXPENSE_REPORT_VIEW')")
    public ResponseEntity<List<ExpenseReportRowDTO>> reportTax(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                               @RequestParam(required = false) Long categoryId,
                                                               @RequestParam(required = false) ExpenseStatus status) {
        return ResponseEntity.ok(service.reportTax(fromDate, toDate, categoryId, status));
    }

    @GetMapping("/reports/monthly")
    @PreAuthorize("hasAuthority('EXPENSE_REPORT_VIEW')")
    public ResponseEntity<List<ExpenseReportRowDTO>> reportMonthly(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                   @RequestParam(required = false) Long categoryId,
                                                                   @RequestParam(required = false) ExpenseStatus status) {
        return ResponseEntity.ok(service.reportMonthly(fromDate, toDate, categoryId, status));
    }
}
