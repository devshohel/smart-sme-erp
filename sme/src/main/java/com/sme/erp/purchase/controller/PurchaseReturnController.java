package com.sme.erp.purchase.controller;

import com.sme.erp.purchase.dto.PurchaseReturnDTO;
import com.sme.erp.purchase.service.PurchaseReturnService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases/returns")
@CrossOrigin(origins = "*")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    public PurchaseReturnController(PurchaseReturnService purchaseReturnService) {
        this.purchaseReturnService = purchaseReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    public ResponseEntity<List<PurchaseReturnDTO>> getAll() {
        return ResponseEntity.ok(purchaseReturnService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    public ResponseEntity<PurchaseReturnDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_CREATE')")
    public ResponseEntity<PurchaseReturnDTO> create(@Valid @RequestBody PurchaseReturnDTO dto) {
        return ResponseEntity.ok(purchaseReturnService.create(dto));
    }
}
