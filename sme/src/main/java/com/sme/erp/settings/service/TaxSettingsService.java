package com.sme.erp.settings.service;

import com.sme.erp.settings.dto.TaxSettingsDTO;

public interface TaxSettingsService {
    TaxSettingsDTO get();
    TaxSettingsDTO update(TaxSettingsDTO dto);
}
