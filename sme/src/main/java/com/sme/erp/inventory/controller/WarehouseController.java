package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.WarehouseDTO;
import com.sme.erp.inventory.service.WarehouseService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public ResponseEntity<WarehouseDTO> save(@Valid @RequestBody WarehouseDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('INVENTORY_EDIT')")
    public ResponseEntity<WarehouseDTO> update(@PathVariable Long id, @Valid @RequestBody WarehouseDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<WarehouseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<WarehouseDTO>> getDeleted() {
        return ResponseEntity.ok(service.getDeleted());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<WarehouseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('INVENTORY_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('WAREHOUSE_RESTORE')")
    public ResponseEntity<WarehouseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }
}
