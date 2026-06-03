package com.sme.erp.product.controller;

import com.sme.erp.product.dto.ProductCategoryDTO;
import com.sme.erp.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin("*")
public class ProductCategoryController {

    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) {
        this.service = service;
    }

    // CREATE / UPDATE
    @PostMapping
    public ResponseEntity<ProductCategoryDTO> save(@Valid @RequestBody ProductCategoryDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDTO> update(@PathVariable Long id, @Valid @RequestBody ProductCategoryDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<ProductCategoryDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
