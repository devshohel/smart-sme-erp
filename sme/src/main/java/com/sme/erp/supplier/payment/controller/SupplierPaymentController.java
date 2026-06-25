package com.sme.erp.supplier.payment.controller;

import com.sme.erp.supplier.payment.dto.SupplierPaymentDTO;
import com.sme.erp.supplier.payment.dto.SupplierPaymentPageDTO;
import com.sme.erp.supplier.payment.enums.SupplierPaymentMethod;
import com.sme.erp.supplier.payment.enums.SupplierPaymentStatus;
import com.sme.erp.supplier.payment.service.SupplierPaymentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/supplier-payments")
public class SupplierPaymentController {
    private final SupplierPaymentService service;

    public SupplierPaymentController(SupplierPaymentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPPLIER_PAYMENT_VIEW','SUPPLIER_PAYMENT_CREATE','SUPPLIER_PAYMENT_EDIT')")
    public ResponseEntity<List<SupplierPaymentDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_PAYMENT_VIEW','SUPPLIER_PAYMENT_CREATE','SUPPLIER_PAYMENT_EDIT')")
    public ResponseEntity<SupplierPaymentPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) SupplierPaymentStatus status,
            @RequestParam(required = false) SupplierPaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.searchPage(keyword, supplierId, status, paymentMethod, fromDate, toDate, page, size, sort, direction));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_PAYMENT_VIEW','SUPPLIER_PAYMENT_CREATE','SUPPLIER_PAYMENT_EDIT')")
    public ResponseEntity<SupplierPaymentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_PAYMENT_CREATE')")
    public ResponseEntity<SupplierPaymentDTO> create(@Valid @RequestBody SupplierPaymentDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_PAYMENT_EDIT')")
    public ResponseEntity<SupplierPaymentDTO> update(@PathVariable Long id, @Valid @RequestBody SupplierPaymentDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('SUPPLIER_PAYMENT_POST')")
    public ResponseEntity<SupplierPaymentDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SUPPLIER_PAYMENT_CANCEL')")
    public ResponseEntity<SupplierPaymentDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority('SUPPLIER_PAYMENT_CANCEL')")
    public ResponseEntity<SupplierPaymentDTO> reverse(@PathVariable Long id, @RequestBody(required = false) SupplierPaymentDTO dto) {
        return ResponseEntity.ok(service.reverse(id, dto == null ? null : dto.getReversalReason()));
    }
}
