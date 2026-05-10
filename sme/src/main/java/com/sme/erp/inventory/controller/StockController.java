package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import com.sme.erp.inventory.service.StockService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/stocks")
@Validated
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    // STOCK IN
    @PostMapping("/in")
    public StockDTO stockIn(@RequestParam @NotNull @Positive Long productId,
                            @RequestParam @NotNull @Positive Long warehouseId,
                            @RequestParam @NotNull @Positive BigDecimal qty,
                            @RequestParam @NotNull @Positive BigDecimal unitCost) {
        return service.stockIn(productId, warehouseId, qty, unitCost);
    }

    // STOCK OUT
    @PostMapping("/out")
    public StockDTO stockOut(@RequestParam @NotNull @Positive Long productId,
                             @RequestParam @NotNull @Positive Long warehouseId,
                             @RequestParam @NotNull @Positive BigDecimal qty) {
        return service.stockOut(productId, warehouseId, qty);
    }

    // GET STOCK
    @GetMapping
    public StockDTO getStock(@RequestParam @NotNull @Positive Long productId,
                            @RequestParam @NotNull @Positive Long warehouseId) {
        return service.getStock(productId, warehouseId);
    }
}
