package com.sme.erp.product.controller;

import com.sme.erp.product.dto.ProductCategoryDTO;
import com.sme.erp.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class ProductCategoryController {

    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) {
        this.service = service;
    }

    // CREATE / UPDATE
    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<ProductCategoryDTO> save(@Valid @RequestBody ProductCategoryDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('CATEGORY_EDIT')")
    public ResponseEntity<ProductCategoryDTO> update(@PathVariable Long id, @Valid @RequestBody ProductCategoryDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ResponseEntity<List<ProductCategoryDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ResponseEntity<List<ProductCategoryDTO>> getDeleted() {
        return ResponseEntity.ok(service.getDeleted());
    }

    // DELETE
    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('CATEGORY_RESTORE')")
    public ResponseEntity<ProductCategoryDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }
}
