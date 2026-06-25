package com.sme.erp.purchase.controller;

import com.sme.erp.purchase.dto.PurchaseReceiveDTO;
import com.sme.erp.purchase.service.PurchaseReceiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases/receives")
public class PurchaseReceiveController {

    private final PurchaseReceiveService purchaseReceiveService;

    public PurchaseReceiveController(PurchaseReceiveService purchaseReceiveService) {
        this.purchaseReceiveService = purchaseReceiveService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_RECEIVE_VIEW')")
    public ResponseEntity<List<PurchaseReceiveDTO>> getAll() {
        return ResponseEntity.ok(purchaseReceiveService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_RECEIVE_VIEW')")
    public ResponseEntity<PurchaseReceiveDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReceiveService.getById(id));
    }
}
