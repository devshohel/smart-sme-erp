package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.WarehouseDTO;
import com.sme.erp.inventory.service.WarehouseService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @PostMapping
    public WarehouseDTO save(@Valid @RequestBody WarehouseDTO dto) {
        return service.save(dto);
    }

    @GetMapping
    public List<WarehouseDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public WarehouseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
