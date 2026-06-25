package com.sme.erp.product.controller;

import com.sme.erp.product.dto.UomDTO;

import com.sme.erp.product.service.UomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('UOM_CREATE')")
    public ResponseEntity<UomDTO> create(@Valid @RequestBody UomDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('UOM_EDIT')")
    public ResponseEntity<UomDTO> update(@PathVariable Long id, @Valid @RequestBody UomDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    // Get All UOMs
    @GetMapping
    @PreAuthorize("hasAuthority('UOM_VIEW')")
    public ResponseEntity<List<UomDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('UOM_VIEW')")
    public ResponseEntity<List<UomDTO>> getDeleted() {
        return ResponseEntity.ok(service.getDeleted());
    }
    
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('UOM_VIEW')")
    public ResponseEntity<UomDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Delete UOM
    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('UOM_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('UOM_RESTORE')")
    public ResponseEntity<UomDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }
}
