package com.sme.erp.settings.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.enums.Status;
import com.sme.erp.settings.dto.TaxSettingsDTO;
import com.sme.erp.settings.entity.TaxSettings;
import com.sme.erp.settings.repository.TaxSettingsRepository;
import com.sme.erp.settings.service.TaxSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TaxSettingsServiceImpl implements TaxSettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final TaxSettingsRepository repository;

    public TaxSettingsServiceImpl(TaxSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TaxSettingsDTO get() {
        return toDto(getOrCreate());
    }

    @Override
    @Transactional
    public TaxSettingsDTO update(TaxSettingsDTO dto) {
        TaxSettings settings = getOrCreate();
        BigDecimal rate = dto.getTaxRate() != null ? dto.getTaxRate() : BigDecimal.ZERO;
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Tax rate cannot be negative");
        }
        settings.setTaxName(RequestValueUtils.normalizeRequired(dto.getTaxName(), "Tax name"));
        settings.setTaxRate(rate);
        settings.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        settings.setDefaultTaxEnabled(dto.getDefaultTaxEnabled() != null ? dto.getDefaultTaxEnabled() : false);
        return toDto(repository.save(settings));
    }

    private TaxSettings getOrCreate() {
        return repository.findById(SETTINGS_ID).orElseGet(() -> {
            TaxSettings settings = new TaxSettings();
            settings.setId(SETTINGS_ID);
            settings.setTaxName("VAT");
            settings.setTaxRate(BigDecimal.ZERO);
            settings.setStatus(Status.ACTIVE);
            settings.setDefaultTaxEnabled(false);
            return repository.save(settings);
        });
    }

    private TaxSettingsDTO toDto(TaxSettings settings) {
        TaxSettingsDTO dto = new TaxSettingsDTO();
        dto.setId(settings.getId());
        dto.setTaxName(settings.getTaxName());
        dto.setTaxRate(settings.getTaxRate());
        dto.setStatus(settings.getStatus());
        dto.setDefaultTaxEnabled(settings.getDefaultTaxEnabled());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }
}
