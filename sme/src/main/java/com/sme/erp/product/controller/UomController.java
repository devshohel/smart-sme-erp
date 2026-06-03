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

    @PutMapping("/{id}")
    public ResponseEntity<UomDTO> update(@PathVariable Long id, @Valid @RequestBody UomDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    // Get All UOMs
    @GetMapping
    public ResponseEntity<List<UomDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UomDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Delete UOM
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
