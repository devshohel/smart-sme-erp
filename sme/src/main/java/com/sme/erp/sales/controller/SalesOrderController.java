package com.sme.erp.sales.controller;

import com.sme.erp.sales.dto.SalesActionReasonDTO;
import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_ORDER_VIEW')")
    public ResponseEntity<List<SalesOrderDTO>> getAll() {
        return ResponseEntity.ok(salesOrderService.getAll());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SALES_ORDER_VIEW')")
    public ResponseEntity<SalesOrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_ORDER_CREATE')")
    public ResponseEntity<SalesOrderDTO> create(@Valid @RequestBody SalesOrderDTO dto) {
        return ResponseEntity.ok(salesOrderService.create(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SALES_ORDER_EDIT')")
    public ResponseEntity<SalesOrderDTO> update(@PathVariable Long id, @Valid @RequestBody SalesOrderDTO dto) {
        return ResponseEntity.ok(salesOrderService.update(id, dto));
    }

    @PostMapping("/{id:\\d+}/submit")
    @PreAuthorize("hasAuthority('SALES_ORDER_SUBMIT')")
    public ResponseEntity<SalesOrderDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.submit(id));
    }

    @PostMapping("/{id:\\d+}/approve")
    @PreAuthorize("hasAuthority('SALES_ORDER_APPROVE')")
    public ResponseEntity<SalesOrderDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.approve(id));
    }

    @PostMapping("/{id:\\d+}/reject")
    @PreAuthorize("hasAuthority('SALES_ORDER_REJECT')")
    public ResponseEntity<SalesOrderDTO> reject(@PathVariable Long id, @Valid @RequestBody SalesActionReasonDTO request) {
        return ResponseEntity.ok(salesOrderService.reject(id, request.getReason()));
    }

    @PostMapping("/{id:\\d+}/cancel")
    @PreAuthorize("hasAuthority('SALES_ORDER_CANCEL')")
    public ResponseEntity<SalesOrderDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.cancel(id));
    }

    @PostMapping("/{id:\\d+}/convert-to-invoice")
    @PreAuthorize("hasAuthority('SALES_ORDER_CONVERT')")
    public ResponseEntity<SalesInvoiceDTO> convertToInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.convertToInvoice(id));
    }
}
