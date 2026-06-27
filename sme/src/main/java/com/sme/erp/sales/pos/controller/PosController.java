package com.sme.erp.sales.pos.controller;

import com.sme.erp.sales.pos.dto.PosCompleteRequestDTO;
import com.sme.erp.sales.pos.dto.PosCompleteResponseDTO;
import com.sme.erp.sales.pos.service.PosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/pos")
public class PosController {
    private final PosService posService;

    public PosController(PosService posService) {
        this.posService = posService;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasAuthority('SALES_INVOICE_CREATE')")
    public ResponseEntity<PosCompleteResponseDTO> complete(@Valid @RequestBody PosCompleteRequestDTO request) {
        return ResponseEntity.ok(posService.complete(request));
    }
}
