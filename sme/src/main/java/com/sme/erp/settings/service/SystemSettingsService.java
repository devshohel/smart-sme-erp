package com.sme.erp.settings.service;

import com.sme.erp.settings.dto.SystemSettingsDTO;

public interface SystemSettingsService {
    SystemSettingsDTO get();
    SystemSettingsDTO update(SystemSettingsDTO dto);
}
