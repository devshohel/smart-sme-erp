package com.sme.erp.sales.controller;

import com.sme.erp.sales.dto.SalesActionReasonDTO;
import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.service.SalesReturnService;
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
@RequestMapping("/api/v1/sales/returns")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    public SalesReturnController(SalesReturnService salesReturnService) {
        this.salesReturnService = salesReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_RETURN_VIEW')")
    public ResponseEntity<List<SalesReturnDTO>> getAll() {
        return ResponseEntity.ok(salesReturnService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_RETURN_VIEW')")
    public ResponseEntity<SalesReturnDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_RETURN_CREATE')")
    public ResponseEntity<SalesReturnDTO> create(@Valid @RequestBody SalesReturnDTO dto) {
        return ResponseEntity.ok(salesReturnService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_RETURN_EDIT')")
    public ResponseEntity<SalesReturnDTO> update(@PathVariable Long id, @Valid @RequestBody SalesReturnDTO dto) {
        return ResponseEntity.ok(salesReturnService.update(id, dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('SALES_RETURN_SUBMIT')")
    public ResponseEntity<SalesReturnDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('SALES_RETURN_APPROVE')")
    public ResponseEntity<SalesReturnDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('SALES_RETURN_REJECT')")
    public ResponseEntity<SalesReturnDTO> reject(@PathVariable Long id, @Valid @RequestBody SalesActionReasonDTO request) {
        return ResponseEntity.ok(salesReturnService.reject(id, request.getReason()));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('SALES_RETURN_POST')")
    public ResponseEntity<SalesReturnDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.post(id));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority('SALES_RETURN_REVERSE')")
    public ResponseEntity<SalesReturnDTO> reverse(@PathVariable Long id, @Valid @RequestBody SalesActionReasonDTO request) {
        return ResponseEntity.ok(salesReturnService.reverse(id, request.getReason()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES_RETURN_CANCEL')")
    public ResponseEntity<SalesReturnDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.cancel(id));
    }
}
