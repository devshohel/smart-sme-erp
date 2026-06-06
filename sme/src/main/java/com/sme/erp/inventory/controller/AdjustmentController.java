package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import com.sme.erp.inventory.service.StockService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/adjustments")
@CrossOrigin(origins = "*")
@Validated
public class AdjustmentController {

    private final StockService service;

    public AdjustmentController(StockService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<StockDTO> adjustStock(@RequestParam @NotNull @Positive Long productId,
                                                @RequestParam @NotNull @Positive Long warehouseId,
                                                @RequestParam @NotNull BigDecimal qty,
                                                @RequestParam @NotBlank String reason) {

        return ResponseEntity.ok(service.adjustStock(productId, warehouseId, qty, reason));
    }
}
