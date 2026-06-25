package com.sme.erp.product.controller;

import com.sme.erp.product.dto.ProductBrandDTO;
import com.sme.erp.product.service.ProductBrandService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class ProductBrandController {

    private final ProductBrandService service;

    public ProductBrandController(ProductBrandService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    public ResponseEntity<ProductBrandDTO> save(@Valid @RequestBody ProductBrandDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('BRAND_EDIT')")
    public ResponseEntity<ProductBrandDTO> update(@PathVariable Long id, @Valid @RequestBody ProductBrandDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BRAND_VIEW')")
    public ResponseEntity<List<ProductBrandDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('BRAND_VIEW')")
    public ResponseEntity<List<ProductBrandDTO>> getDeleted() {
        return ResponseEntity.ok(service.getDeleted());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('BRAND_VIEW')")
    public ResponseEntity<ProductBrandDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('BRAND_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('BRAND_RESTORE')")
    public ResponseEntity<ProductBrandDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }
}
