package com.sme.erp.accounting.controller;

import com.sme.erp.accounting.dto.JournalEntryDTO;
import com.sme.erp.accounting.enums.JournalStatus;
import com.sme.erp.accounting.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting/journal-entries")
@CrossOrigin("*")
public class JournalEntryController {
    private final JournalEntryService service;

    public JournalEntryController(JournalEntryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<List<JournalEntryDTO>> getAll(@RequestParam(required = false) JournalStatus status) {
        return ResponseEntity.ok(service.getAll(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING_VIEW')")
    public ResponseEntity<JournalEntryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_CREATE')")
    public ResponseEntity<JournalEntryDTO> create(@Valid @RequestBody JournalEntryDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAuthority('JOURNAL_POST') or hasAuthority('ACCOUNTING_POST')")
    public ResponseEntity<JournalEntryDTO> post(@PathVariable Long id) {
        return ResponseEntity.ok(service.post(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTING_EDIT')")
    public ResponseEntity<JournalEntryDTO> update(@PathVariable Long id, @Valid @RequestBody JournalEntryDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('JOURNAL_CANCEL')")
    public ResponseEntity<JournalEntryDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}
