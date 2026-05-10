package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockMovementDTO;
import com.sme.erp.inventory.service.StockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movements")
public class MovementController {

    private final StockService service;

    public MovementController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public List<StockMovementDTO> getAll() {
        return service.getAllMovements();
    }
}
