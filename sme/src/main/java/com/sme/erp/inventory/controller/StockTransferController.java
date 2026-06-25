package com.sme.erp.inventory.controller;

import com.sme.erp.inventory.dto.StockTransferDTO;
import com.sme.erp.inventory.dto.StockTransferPageDTO;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.inventory.service.StockTransferService;
import com.sme.erp.purchase.dto.PurchaseActionReasonDTO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/transfers")
public class StockTransferController {

    private final StockTransferService service;

    public StockTransferController(StockTransferService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TRANSFER_VIEW','TRANSFER_EDIT')")
    public ResponseEntity<List<StockTransferDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('TRANSFER_VIEW','TRANSFER_EDIT')")
    public ResponseEntity<StockTransferPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long fromWarehouseId,
            @RequestParam(required = false) Long toWarehouseId,
            @RequestParam(required = false) StockTransferStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.search(keyword, fromWarehouseId, toWarehouseId, status, fromDate, toDate,
                page, size, sort, direction));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TRANSFER_VIEW','TRANSFER_EDIT')")
    public ResponseEntity<StockTransferDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRANSFER_CREATE')")
    public ResponseEntity<StockTransferDTO> create(@Valid @RequestBody StockTransferDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSFER_EDIT')")
    public ResponseEntity<StockTransferDTO> update(@PathVariable Long id, @Valid @RequestBody StockTransferDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('TRANSFER_APPROVE')")
    public ResponseEntity<StockTransferDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('TRANSFER_SEND')")
    public ResponseEntity<StockTransferDTO> send(@PathVariable Long id) {
        return ResponseEntity.ok(service.send(id));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('TRANSFER_RECEIVE')")
    public ResponseEntity<StockTransferDTO> receive(@PathVariable Long id) {
        return ResponseEntity.ok(service.receive(id));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority('TRANSFER_REVERSE')")
    public ResponseEntity<StockTransferDTO> reverse(@PathVariable Long id, @Valid @RequestBody PurchaseActionReasonDTO request) {
        return ResponseEntity.ok(service.reverse(id, request.getReason()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('TRANSFER_CANCEL')")
    public ResponseEntity<StockTransferDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}
