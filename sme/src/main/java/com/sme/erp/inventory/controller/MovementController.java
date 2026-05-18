package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<StockMovementDTO>> getAll() {
        return ResponseEntity.ok(service.getAllMovements());
    }
}
