package com.sme.erp.product.controller;

import com.sme.erp.product.dto.ProductBrandDTO;
import com.sme.erp.product.service.ProductBrandService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@CrossOrigin(origins = "*")
public class ProductBrandController {

    private final ProductBrandService service;

    public ProductBrandController(ProductBrandService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductBrandDTO> save(@Valid @RequestBody ProductBrandDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductBrandDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductBrandDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}