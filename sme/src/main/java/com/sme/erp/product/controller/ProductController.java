package com.sme.erp.product.controller;

import com.sme.erp.enums.Status;
import com.sme.erp.product.dto.ProductBulkStatusUpdateDTO;
import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.dto.ProductPageDTO;
import com.sme.erp.product.dto.ProductStatsDTO;
import com.sme.erp.product.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.saveProduct(dto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductDTO> createProductWithImage(
            @Valid @RequestPart("product") ProductDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(service.saveProduct(dto, image));
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_EDIT')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.saveProduct(dto));
    }

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_EDIT')")
    public ResponseEntity<ProductDTO> updateProductWithImage(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        dto.setId(id);
        return ResponseEntity.ok(service.saveProduct(dto, image));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<List<ProductDTO>> getDeleted() {
        return ResponseEntity.ok(service.getDeletedProducts());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ProductPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(service.searchProducts(keyword, categoryId, brandId, status, page, size, sort, direction));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ProductStatsDTO> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    @PutMapping("/bulk-status")
    @PreAuthorize("hasAuthority('PRODUCT_EDIT')")
    public ResponseEntity<Integer> updateStatusBulk(@Valid @RequestBody ProductBulkStatusUpdateDTO dto) {
        return ResponseEntity.ok(service.updateStatusBulk(dto.getProductIds(), dto.getStatus()));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('PRODUCT_RESTORE')")
    public ResponseEntity<ProductDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }
}
