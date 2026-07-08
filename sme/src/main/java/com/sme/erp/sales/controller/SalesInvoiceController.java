package com.sme.erp.sales.controller;

import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.service.SalesInvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;

    public SalesInvoiceController(SalesInvoiceService salesInvoiceService) {
        this.salesInvoiceService = salesInvoiceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_INVOICE_VIEW')")
    public ResponseEntity<List<SalesInvoiceDTO>> getAll() {
        return ResponseEntity.ok(salesInvoiceService.getAll());
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAuthority('SALES_INVOICE_VIEW')")
    public ResponseEntity<List<SalesInvoiceDTO>> getUnpaidByCustomerId(@RequestParam Long customerId) {
        return ResponseEntity.ok(salesInvoiceService.getUnpaidByCustomerId(customerId));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SALES_INVOICE_VIEW')")
    public ResponseEntity<SalesInvoiceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_INVOICE_CREATE')")
    public ResponseEntity<SalesInvoiceDTO> create(@Valid @RequestBody SalesInvoiceDTO dto) {
        return ResponseEntity.ok(salesInvoiceService.create(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SALES_INVOICE_EDIT')")
    public ResponseEntity<SalesInvoiceDTO> update(@PathVariable Long id, @Valid @RequestBody SalesInvoiceDTO dto) {
        return ResponseEntity.ok(salesInvoiceService.update(id, dto));
    }

    @PostMapping("/{id:\\d+}/submit")
    @PreAuthorize("hasAuthority('SALES_INVOICE_SUBMIT')")
    public ResponseEntity<SalesInvoiceDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.submit(id));
    }

    @PostMapping("/{id:\\d+}/approve")
    @PreAuthorize("hasAuthority('SALES_INVOICE_APPROVE')")
    public ResponseEntity<SalesInvoiceDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.approve(id));
    }

    @PostMapping("/{id:\\d+}/post")
    @PreAuthorize("hasAuthority('SALES_INVOICE_POST')")
    public ResponseEntity<SalesInvoiceDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.post(id));
    }

    @PostMapping("/{id:\\d+}/cancel")
    @PreAuthorize("hasAuthority('SALES_INVOICE_CANCEL')")
    public ResponseEntity<SalesInvoiceDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(salesInvoiceService.cancel(id));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('SALES_INVOICE_CANCEL','SALES_DELETE')")
    public ResponseEntity<Void> deleteDraft(@PathVariable Long id) {
        salesInvoiceService.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }

}
