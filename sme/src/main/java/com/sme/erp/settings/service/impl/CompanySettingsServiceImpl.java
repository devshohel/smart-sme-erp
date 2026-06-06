package com.sme.erp.settings.service.impl;

import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import com.sme.erp.settings.dto.CompanySettingsDTO;
import com.sme.erp.settings.entity.CompanySettings;
import com.sme.erp.settings.repository.CompanySettingsRepository;
import com.sme.erp.settings.service.CompanySettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanySettingsServiceImpl implements CompanySettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final CompanySettingsRepository repository;

    public CompanySettingsServiceImpl(CompanySettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CompanySettingsDTO get() {
        return toDto(getOrCreate());
    }

    @Override
    @Transactional
    public CompanySettingsDTO update(CompanySettingsDTO dto) {
        CompanySettings settings = getOrCreate();
        settings.setCompanyName(RequestValueUtils.normalizeRequired(dto.getCompanyName(), "Company name"));
        settings.setBusinessType(RequestValueUtils.normalize(dto.getBusinessType()));
        settings.setEmail(RequestValueUtils.normalize(dto.getEmail()));
        settings.setPhone(RequestValueUtils.normalize(dto.getPhone()));
        settings.setAddress(RequestValueUtils.normalize(dto.getAddress()));
        settings.setCity(RequestValueUtils.normalize(dto.getCity()));
        settings.setCountry(RequestValueUtils.normalize(dto.getCountry()));
        settings.setLogoUrl(RequestValueUtils.normalize(dto.getLogoUrl()));
        settings.setTaxNumber(RequestValueUtils.normalize(dto.getTaxNumber()));
        settings.setCurrency(RequestValueUtils.normalizeRequired(dto.getCurrency(), "Currency"));
        settings.setTimezone(RequestValueUtils.normalizeRequired(dto.getTimezone(), "Timezone"));
        settings.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        return toDto(repository.save(settings));
    }

    private CompanySettings getOrCreate() {
        return repository.findById(SETTINGS_ID).orElseGet(() -> {
            CompanySettings settings = new CompanySettings();
            settings.setId(SETTINGS_ID);
            settings.setCompanyName("Smart SME ERP");
            settings.setBusinessType("SME");
            settings.setEmail("admin@nexaone.local");
            settings.setPhone("");
            settings.setAddress("");
            settings.setCity("");
            settings.setCountry("Bangladesh");
            settings.setLogoUrl("");
            settings.setTaxNumber("");
            settings.setCurrency("BDT");
            settings.setTimezone("Asia/Dhaka");
            settings.setStatus(Status.ACTIVE);
            return repository.save(settings);
        });
    }

    private CompanySettingsDTO toDto(CompanySettings settings) {
        CompanySettingsDTO dto = new CompanySettingsDTO();
        dto.setId(settings.getId());
        dto.setCompanyName(settings.getCompanyName());
        dto.setBusinessType(settings.getBusinessType());
        dto.setEmail(settings.getEmail());
        dto.setPhone(settings.getPhone());
        dto.setAddress(settings.getAddress());
        dto.setCity(settings.getCity());
        dto.setCountry(settings.getCountry());
        dto.setLogoUrl(settings.getLogoUrl());
        dto.setTaxNumber(settings.getTaxNumber());
        dto.setCurrency(settings.getCurrency());
        dto.setTimezone(settings.getTimezone());
        dto.setStatus(settings.getStatus());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }
}
