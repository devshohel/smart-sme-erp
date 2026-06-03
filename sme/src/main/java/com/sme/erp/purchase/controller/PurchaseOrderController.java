package com.sme.erp.purchase.controller;

import com.sme.erp.purchase.dto.PurchaseOrderDTO;
import com.sme.erp.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/purchases/orders")
@CrossOrigin(origins = "*")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTO>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> create(@Valid @RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderDTO dto) {
        return ResponseEntity.ok(purchaseOrderService.update(id, dto));
    }
}
