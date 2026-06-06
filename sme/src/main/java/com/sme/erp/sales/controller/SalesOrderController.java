package com.sme.erp.sales.controller;

import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.service.SalesOrderService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/orders")
@CrossOrigin(origins = "*")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<List<SalesOrderDTO>> getAll() {
        return ResponseEntity.ok(salesOrderService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<SalesOrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<SalesOrderDTO> create(@Valid @RequestBody SalesOrderDTO dto) {
        return ResponseEntity.ok(salesOrderService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_EDIT')")
    public ResponseEntity<SalesOrderDTO> update(@PathVariable Long id, @Valid @RequestBody SalesOrderDTO dto) {
        return ResponseEntity.ok(salesOrderService.update(id, dto));
    }
}
