package com.sme.erp.purchase.controller;

import com.sme.erp.purchase.dto.PurchaseActionReasonDTO;
import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.service.PurchaseReturnService;
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
@RequestMapping("/api/v1/purchases/returns")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    public PurchaseReturnController(PurchaseReturnService purchaseReturnService) {
        this.purchaseReturnService = purchaseReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_VIEW')")
    public ResponseEntity<List<PurchaseReturnDTO>> getAll() {
        return ResponseEntity.ok(purchaseReturnService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_VIEW')")
    public ResponseEntity<PurchaseReturnDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_CREATE')")
    public ResponseEntity<PurchaseReturnDTO> create(@Valid @RequestBody PurchaseReturnDTO dto) {
        return ResponseEntity.ok(purchaseReturnService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_EDIT')")
    public ResponseEntity<PurchaseReturnDTO> update(@PathVariable Long id, @Valid @RequestBody PurchaseReturnDTO dto) {
        return ResponseEntity.ok(purchaseReturnService.update(id, dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_SUBMIT')")
    public ResponseEntity<PurchaseReturnDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_APPROVE')")
    public ResponseEntity<PurchaseReturnDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_REJECT')")
    public ResponseEntity<PurchaseReturnDTO> reject(@PathVariable Long id, @Valid @RequestBody PurchaseActionReasonDTO request) {
        return ResponseEntity.ok(purchaseReturnService.reject(id, request.getReason()));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_POST')")
    public ResponseEntity<PurchaseReturnDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.post(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PURCHASE_RETURN_CANCEL')")
    public ResponseEntity<PurchaseReturnDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.cancel(id));
    }
}
