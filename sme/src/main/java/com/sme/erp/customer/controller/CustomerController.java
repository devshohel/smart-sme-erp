package com.sme.erp.customer.controller;

import com.sme.erp.customer.dto.CustomerAgingReportDTO;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.dto.CustomerDetailDTO;
import com.sme.erp.customer.dto.CustomerLedgerDTO;
import com.sme.erp.customer.dto.CustomerOptionDTO;
import com.sme.erp.customer.dto.CustomerPageDTO;
import com.sme.erp.customer.service.CustomerService;
import com.sme.erp.enums.Status;
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
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT')")
    public ResponseEntity<List<CustomerDTO>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(customerService.getAll(keyword, status));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT')")
    public ResponseEntity<List<CustomerDTO>> getDeleted() {
        return ResponseEntity.ok(customerService.getDeleted());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT')")
    public ResponseEntity<CustomerPageDTO> getPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(customerService.searchPage(keyword, status, page, size, sort, direction));
    }

    @GetMapping("/{id:\\d+}/detail")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT')")
    public ResponseEntity<CustomerDetailDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getDetail(id));
    }

    @GetMapping("/{id:\\d+}/ledger")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT')")
    public ResponseEntity<CustomerLedgerDTO> getLedger(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(customerService.getLedger(id, fromDate, toDate));
    }

    @GetMapping("/aging")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT','CUSTOMER_AGING_VIEW')")
    public ResponseEntity<CustomerAgingReportDTO> getAging(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(customerService.getAging(customerId, fromDate, toDate));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT')")
    public ResponseEntity<CustomerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('CUSTOMER_VIEW','CUSTOMER_LEDGER_VIEW','CUSTOMER_EDIT','CUSTOMER_AGING_VIEW')")
    public ResponseEntity<List<CustomerOptionDTO>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(customerService.autocomplete(keyword));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    public ResponseEntity<CustomerDTO> create(@Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.create(dto));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('CUSTOMER_EDIT')")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.update(id, dto));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasAuthority('CUSTOMER_RESTORE')")
    public ResponseEntity<CustomerDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.restore(id));
    }
}
