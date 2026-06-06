package com.sme.erp.settings.service;

import com.sme.erp.settings.dto.InvoiceSettingsDTO;

public interface InvoiceSettingsService {
    InvoiceSettingsDTO get();
    InvoiceSettingsDTO update(InvoiceSettingsDTO dto);
}
