package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockAdjustmentDTO;
import com.sme.erp.inventory.service.StockAdjustmentService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/adjustments")
@Validated
public class AdjustmentController {

    private final StockAdjustmentService service;

    public AdjustmentController(StockAdjustmentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_ADJUSTMENT_VIEW')")
    public ResponseEntity<List<StockAdjustmentDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_ADJUSTMENT_VIEW')")
    public ResponseEntity<StockAdjustmentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STOCK_ADJUSTMENT_CREATE')")
    public ResponseEntity<StockAdjustmentDTO> create(@RequestParam @NotNull @Positive Long productId,
                                                     @RequestParam @NotNull @Positive Long warehouseId,
                                                     @RequestParam @NotNull BigDecimal qty,
                                                     @RequestParam @NotBlank String reason) {

        return ResponseEntity.ok(service.create(productId, warehouseId, qty, reason));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('STOCK_ADJUSTMENT_APPROVE')")
    public ResponseEntity<StockAdjustmentDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('STOCK_ADJUSTMENT_POST')")
    public ResponseEntity<StockAdjustmentDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('STOCK_ADJUSTMENT_CANCEL')")
    public ResponseEntity<StockAdjustmentDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

}
