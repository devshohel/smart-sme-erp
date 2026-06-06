package com.sme.erp.settings.controller;

import com.sme.erp.settings.dto.CompanySettingsDTO;
import com.sme.erp.settings.dto.InvoiceSettingsDTO;
import com.sme.erp.settings.dto.SystemSettingsDTO;
import com.sme.erp.settings.dto.TaxSettingsDTO;
import com.sme.erp.settings.service.CompanySettingsService;
import com.sme.erp.settings.service.InvoiceSettingsService;
import com.sme.erp.settings.service.SystemSettingsService;
import com.sme.erp.settings.service.TaxSettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@CrossOrigin(origins = "*")
public class SettingsController {
    private final CompanySettingsService companySettingsService;
    private final InvoiceSettingsService invoiceSettingsService;
    private final TaxSettingsService taxSettingsService;
    private final SystemSettingsService systemSettingsService;

    public SettingsController(
            CompanySettingsService companySettingsService,
            InvoiceSettingsService invoiceSettingsService,
            TaxSettingsService taxSettingsService,
            SystemSettingsService systemSettingsService) {
        this.companySettingsService = companySettingsService;
        this.invoiceSettingsService = invoiceSettingsService;
        this.taxSettingsService = taxSettingsService;
        this.systemSettingsService = systemSettingsService;
    }

    @GetMapping("/company")
    @PreAuthorize("hasAuthority('SETTINGS_VIEW')")
    public CompanySettingsDTO getCompany() {
        return companySettingsService.get();
    }

    @PutMapping("/company")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('SETTINGS_EDIT')")
    public CompanySettingsDTO updateCompany(@RequestBody CompanySettingsDTO dto) {
        return companySettingsService.update(dto);
    }

    @GetMapping("/invoice")
    @PreAuthorize("hasAuthority('SETTINGS_VIEW')")
    public InvoiceSettingsDTO getInvoice() {
        return invoiceSettingsService.get();
    }

    @PutMapping("/invoice")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('SETTINGS_EDIT')")
    public InvoiceSettingsDTO updateInvoice(@RequestBody InvoiceSettingsDTO dto) {
        return invoiceSettingsService.update(dto);
    }

    @GetMapping("/tax")
    @PreAuthorize("hasAuthority('SETTINGS_VIEW')")
    public TaxSettingsDTO getTax() {
        return taxSettingsService.get();
    }

    @PutMapping("/tax")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('SETTINGS_EDIT')")
    public TaxSettingsDTO updateTax(@RequestBody TaxSettingsDTO dto) {
        return taxSettingsService.update(dto);
    }

    @GetMapping("/system")
    @PreAuthorize("hasAuthority('SETTINGS_VIEW')")
    public SystemSettingsDTO getSystem() {
        return systemSettingsService.get();
    }

    @PutMapping("/system")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('SETTINGS_EDIT')")
    public SystemSettingsDTO updateSystem(@RequestBody SystemSettingsDTO dto) {
        return systemSettingsService.update(dto);
    }
}
