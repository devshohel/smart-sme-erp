package com.sme.erp.sales.controller;

import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.service.SalesReturnService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales/returns")
@CrossOrigin(origins = "*")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    public SalesReturnController(SalesReturnService salesReturnService) {
        this.salesReturnService = salesReturnService;
    }

    @GetMapping
    public ResponseEntity<List<SalesReturnDTO>> getAll() {
        return ResponseEntity.ok(salesReturnService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesReturnDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesReturnService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SalesReturnDTO> create(@Valid @RequestBody SalesReturnDTO dto) {
        return ResponseEntity.ok(salesReturnService.create(dto));
    }
}
