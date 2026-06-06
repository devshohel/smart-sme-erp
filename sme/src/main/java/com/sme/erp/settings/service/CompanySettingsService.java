package com.sme.erp.settings.service;

import com.sme.erp.settings.dto.CompanySettingsDTO;

public interface CompanySettingsService {
    CompanySettingsDTO get();
    CompanySettingsDTO update(CompanySettingsDTO dto);
}
