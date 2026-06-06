package com.sme.erp.settings.service.impl;

import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.settings.dto.SystemSettingsDTO;
import com.sme.erp.settings.entity.SystemSettings;
import com.sme.erp.settings.repository.SystemSettingsRepository;
import com.sme.erp.settings.service.SystemSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemSettingsServiceImpl implements SystemSettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final SystemSettingsRepository repository;

    public SystemSettingsServiceImpl(SystemSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SystemSettingsDTO get() {
        return toDto(getOrCreate());
    }

    @Override
    @Transactional
    public SystemSettingsDTO update(SystemSettingsDTO dto) {
        SystemSettings settings = getOrCreate();
        settings.setDefaultCurrency(RequestValueUtils.normalizeRequired(dto.getDefaultCurrency(), "Default currency"));
        settings.setDateFormat(RequestValueUtils.normalizeRequired(dto.getDateFormat(), "Date format"));
        settings.setNumberFormat(RequestValueUtils.normalizeRequired(dto.getNumberFormat(), "Number format"));
        settings.setLowStockAlertEnabled(dto.getLowStockAlertEnabled() != null ? dto.getLowStockAlertEnabled() : true);
        settings.setDashboardRefreshEnabled(dto.getDashboardRefreshEnabled() != null ? dto.getDashboardRefreshEnabled() : true);
        return toDto(repository.save(settings));
    }

    private SystemSettings getOrCreate() {
        return repository.findById(SETTINGS_ID).orElseGet(() -> {
            SystemSettings settings = new SystemSettings();
            settings.setId(SETTINGS_ID);
            settings.setDefaultCurrency("BDT");
            settings.setDateFormat("yyyy-MM-dd");
            settings.setNumberFormat("#,##0.00");
            settings.setLowStockAlertEnabled(true);
            settings.setDashboardRefreshEnabled(true);
            return repository.save(settings);
        });
    }

    private SystemSettingsDTO toDto(SystemSettings settings) {
        SystemSettingsDTO dto = new SystemSettingsDTO();
        dto.setId(settings.getId());
        dto.setDefaultCurrency(settings.getDefaultCurrency());
        dto.setDateFormat(settings.getDateFormat());
        dto.setNumberFormat(settings.getNumberFormat());
        dto.setLowStockAlertEnabled(settings.getLowStockAlertEnabled());
        dto.setDashboardRefreshEnabled(settings.getDashboardRefreshEnabled());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }
}
