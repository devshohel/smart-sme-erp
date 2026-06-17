package com.sme.erp.customer.receipt.controller;

import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.dto.CustomerReceiptPageDTO;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.enums.CustomerReceiptStatus;
import com.sme.erp.customer.receipt.service.CustomerReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-receipts")
@CrossOrigin(origins = "*")
public class CustomerReceiptController {
    private final CustomerReceiptService service;

    public CustomerReceiptController(CustomerReceiptService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    public ResponseEntity<List<CustomerReceiptDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    public ResponseEntity<CustomerReceiptPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) CustomerReceiptStatus status,
            @RequestParam(required = false) CustomerReceiptPaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "receiptDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.searchPage(keyword, customerId, status, paymentMethod, fromDate, toDate, page, size, sort, direction));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    public ResponseEntity<CustomerReceiptDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    public ResponseEntity<CustomerReceiptDTO> create(@Valid @RequestBody CustomerReceiptDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_EDIT')")
    public ResponseEntity<CustomerReceiptDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerReceiptDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('CUSTOMER_EDIT')")
    public ResponseEntity<CustomerReceiptDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CUSTOMER_EDIT')")
    public ResponseEntity<CustomerReceiptDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}
