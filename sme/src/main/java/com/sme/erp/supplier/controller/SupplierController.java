package com.sme.erp.supplier.controller;

import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.ApReconciliationDTO;
import com.sme.erp.supplier.dto.SupplierDetailDTO;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.dto.SupplierOptionDTO;
import com.sme.erp.supplier.dto.SupplierPageDTO;
import com.sme.erp.supplier.dto.SupplierLedgerDTO;
import com.sme.erp.supplier.dto.SupplierAgingReportDTO;
import com.sme.erp.supplier.dto.SupplierStatementDTO;
import com.sme.erp.supplier.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<List<SupplierDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(supplierService.getAll(keyword, status));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<List<SupplierDTO>> getDeleted() {
        return ResponseEntity.ok(supplierService.getDeleted());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<SupplierPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(supplierService.searchPage(keyword, status, page, size, sort, direction));
    }

    @GetMapping("/{id:\\d+}/detail")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<SupplierDetailDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getDetail(id));
    }

    @GetMapping("/{id:\\d+}/ledger")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_LEDGER_VIEW','SUPPLIER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<SupplierLedgerDTO> getLedger(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(supplierService.getLedger(id, fromDate, toDate));
    }

    @GetMapping("/{id:\\d+}/statement")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_LEDGER_VIEW','SUPPLIER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<SupplierStatementDTO> getStatement(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(supplierService.getStatement(id, fromDate, toDate));
    }

    @GetMapping("/aging")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_LEDGER_VIEW','SUPPLIER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<SupplierAgingReportDTO> getAging(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(supplierService.getAging(supplierId, fromDate, toDate));
    }

    @GetMapping("/ap-reconciliation")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_LEDGER_VIEW','SUPPLIER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<ApReconciliationDTO> getApReconciliation(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(supplierService.getApReconciliation(supplierId, fromDate, toDate));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<List<SupplierOptionDTO>> options(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(supplierService.autocomplete(keyword));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<SupplierDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('SUPPLIER_VIEW','SUPPLIER_LEDGER_VIEW','SUPPLIER_EDIT')")
    public ResponseEntity<List<SupplierDTO>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(supplierService.getAll(keyword, null));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE')")
    public ResponseEntity<SupplierDTO> create(@Valid @RequestBody SupplierDTO dto) {
        return ResponseEntity.ok(supplierService.create(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SUPPLIER_EDIT')")
    public ResponseEntity<SupplierDTO> update(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        return ResponseEntity.ok(supplierService.update(id, dto));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('SUPPLIER_RESTORE')")
    public ResponseEntity<SupplierDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.restore(id));
    }
}
