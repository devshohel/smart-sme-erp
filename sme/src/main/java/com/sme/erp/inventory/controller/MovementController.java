package com.sme.erp.inventory.controller;

import com.sme.erp.enums.MovementType;
import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.dto.StockMovementPageDTO;
import com.sme.erp.inventory.service.StockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movements")
@CrossOrigin(origins = "*")
public class MovementController {

    private final StockService service;

    public MovementController(StockService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<StockMovementDTO>> getAll() {
        return ResponseEntity.ok(service.getAllMovements());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<StockMovementPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.searchMovements(keyword, productId, warehouseId, movementType, referenceType,
                fromDate, toDate, page, size, sort, direction));
    }
}
