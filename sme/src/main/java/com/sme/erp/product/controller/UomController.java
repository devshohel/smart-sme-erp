package com.sme.erp.product.controller;

import com.sme.erp.product.dto.UomDTO;

import com.sme.erp.product.service.UomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uoms")
@CrossOrigin(origins = "*")
public class UomController {

    private final UomService service;

    public UomController(UomService service) {
        this.service = service;
    }

    // Create UOM
    @PostMapping
    public ResponseEntity<UomDTO> create(@Valid @RequestBody UomDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    // Get All UOMs
    @GetMapping
    public List<UomDTO> getAll() {
        return service.getAll();
    }
    
    @GetMapping("/{id}")
    public UomDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // Delete UOM
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}