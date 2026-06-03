package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import com.sme.erp.inventory.service.StockService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@CrossOrigin(origins = "*")
@Validated
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    // STOCK IN
    @PostMapping("/in")
    public ResponseEntity<StockDTO> stockIn(@RequestParam @NotNull @Positive Long productId,
                                            @RequestParam @NotNull @Positive Long warehouseId,
                                            @RequestParam @NotNull @Positive BigDecimal qty,
                                            @RequestParam @NotNull @Positive BigDecimal unitCost) {
        return ResponseEntity.ok(service.stockIn(productId, warehouseId, qty, unitCost));
    }

    // STOCK OUT
    @PostMapping("/out")
    public ResponseEntity<StockDTO> stockOut(@RequestParam @NotNull @Positive Long productId,
                                             @RequestParam @NotNull @Positive Long warehouseId,
                                             @RequestParam @NotNull @Positive BigDecimal qty) {
        return ResponseEntity.ok(service.stockOut(productId, warehouseId, qty));
    }

    // GET STOCK
    @GetMapping
    public ResponseEntity<StockDTO> getStock(@RequestParam @NotNull @Positive Long productId,
                                             @RequestParam @NotNull @Positive Long warehouseId) {
        return ResponseEntity.ok(service.getStock(productId, warehouseId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StockDTO>> getAllStock() {
        return ResponseEntity.ok(service.getAllStock());
    }
}
