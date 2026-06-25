package com.sme.erp.purchase.controller;

import com.sme.erp.purchase.dto.PurchaseActionReasonDTO;
import com.sme.erp.purchase.dto.PurchaseOrderDTO;
import com.sme.erp.purchase.dto.PurchaseReceiveDTO;
import com.sme.erp.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases/orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_VIEW')")
    public ResponseEntity<List<PurchaseOrderDTO>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.getAll());
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_VIEW')")
    public ResponseEntity<List<PurchaseOrderDTO>> getUnpaidBySupplier(@RequestParam Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.getUnpaidBySupplier(supplierId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_VIEW')")
    public ResponseEntity<PurchaseOrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE')")
    public ResponseEntity<PurchaseOrderDTO> create(@Valid @RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_EDIT')")
    public ResponseEntity<PurchaseOrderDTO> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.update(id, dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_SUBMIT')")
    public ResponseEntity<PurchaseOrderDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_APPROVE')")
    public ResponseEntity<PurchaseOrderDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_REJECT')")
    public ResponseEntity<PurchaseOrderDTO> reject(@PathVariable Long id, @Valid @RequestBody PurchaseActionReasonDTO request) {
        return ResponseEntity.ok(purchaseOrderService.reject(id, request.getReason()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CANCEL')")
    public ResponseEntity<PurchaseOrderDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_RECEIVE')")
    public ResponseEntity<PurchaseOrderDTO> receive(@PathVariable Long id, @Valid @RequestBody PurchaseReceiveDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.receive(id, dto));
    }
}
