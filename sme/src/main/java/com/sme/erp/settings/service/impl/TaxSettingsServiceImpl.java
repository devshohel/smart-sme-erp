package com.sme.erp.settings.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    public TaxSettingsServiceImpl(TaxSettingsRepository repository, AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
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
        settings.setTaxReceivableAccount(resolveAccount(dto.getTaxReceivableAccountId()));
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
        if (settings.getTaxReceivableAccount() != null) {
            dto.setTaxReceivableAccountId(settings.getTaxReceivableAccount().getId());
            dto.setTaxReceivableAccountCode(settings.getTaxReceivableAccount().getAccountCode());
            dto.setTaxReceivableAccountName(settings.getTaxReceivableAccount().getAccountName());
        }
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }

    private Account resolveAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax receivable account not found with id: " + accountId));
    }
}
