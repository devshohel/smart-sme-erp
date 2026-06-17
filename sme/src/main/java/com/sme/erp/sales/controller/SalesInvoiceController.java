package com.sme.erp.sales.controller;

import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.service.SalesInvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/invoices")
@CrossOrigin(origins = "*")
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;

    public SalesInvoiceController(SalesInvoiceService salesInvoiceService) {
        this.salesInvoiceService = salesInvoiceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<List<SalesInvoiceDTO>> getAll() {
        return ResponseEntity.ok(salesInvoiceService.getAll());
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<List<SalesInvoiceDTO>> getUnpaidByCustomerId(@RequestParam Long customerId) {
        return ResponseEntity.ok(salesInvoiceService.getUnpaidByCustomerId(customerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<SalesInvoiceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<SalesInvoiceDTO> create(@Valid @RequestBody SalesInvoiceDTO dto) {
        return ResponseEntity.ok(salesInvoiceService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_EDIT')")
    public ResponseEntity<SalesInvoiceDTO> update(@PathVariable Long id, @Valid @RequestBody SalesInvoiceDTO dto) {
        return ResponseEntity.ok(salesInvoiceService.update(id, dto));
    }
}
