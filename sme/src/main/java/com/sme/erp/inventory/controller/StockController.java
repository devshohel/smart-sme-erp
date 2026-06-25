package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockDTO;
import com.sme.erp.inventory.dto.StockCardDTO;
import com.sme.erp.inventory.dto.StockPageDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import com.sme.erp.inventory.service.StockService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

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
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public ResponseEntity<StockDTO> stockIn(@RequestParam @NotNull @Positive Long productId,
                                            @RequestParam @NotNull @Positive Long warehouseId,
                                            @RequestParam @NotNull @Positive BigDecimal qty,
                                            @RequestParam @NotNull @Positive BigDecimal unitCost) {
        return ResponseEntity.ok(service.stockIn(productId, warehouseId, qty, unitCost));
    }

    // STOCK OUT
    @PostMapping("/out")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public ResponseEntity<StockDTO> stockOut(@RequestParam @NotNull @Positive Long productId,
                                             @RequestParam @NotNull @Positive Long warehouseId,
                                             @RequestParam @NotNull @Positive BigDecimal qty) {
        return ResponseEntity.ok(service.stockOut(productId, warehouseId, qty));
    }

    // GET STOCK
    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<StockDTO> getStock(@RequestParam @NotNull @Positive Long productId,
                                             @RequestParam @NotNull @Positive Long warehouseId) {
        return ResponseEntity.ok(service.getStock(productId, warehouseId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<StockDTO>> getAllStock() {
        return ResponseEntity.ok(service.getAllStock());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<StockPageDTO> getStockPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(service.searchStock(keyword, warehouseId, categoryId, lowStockOnly, page, size, sort, direction));
    }

    @GetMapping("/card")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<StockCardDTO> getStockCard(@RequestParam @NotNull @Positive Long productId,
                                                     @RequestParam @NotNull @Positive Long warehouseId) {
        return ResponseEntity.ok(service.getStockCard(productId, warehouseId));
    }
}
