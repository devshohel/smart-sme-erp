package com.sme.erp.settings.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.settings.dto.InvoiceSettingsDTO;
import com.sme.erp.settings.entity.InvoiceSettings;
import com.sme.erp.settings.repository.InvoiceSettingsRepository;
import com.sme.erp.settings.service.InvoiceSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceSettingsServiceImpl implements InvoiceSettingsService {
    private static final Long SETTINGS_ID = 1L;

    private final InvoiceSettingsRepository repository;

    public InvoiceSettingsServiceImpl(InvoiceSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public InvoiceSettingsDTO get() {
        return toDto(getOrCreate());
    }

    @Override
    @Transactional
    public InvoiceSettingsDTO update(InvoiceSettingsDTO dto) {
        InvoiceSettings settings = getOrCreate();
        settings.setSalesInvoicePrefix(RequestValueUtils.normalizeRequired(dto.getSalesInvoicePrefix(), "Sales invoice prefix"));
        settings.setPurchaseInvoicePrefix(RequestValueUtils.normalizeRequired(dto.getPurchaseInvoicePrefix(), "Purchase invoice prefix"));
        settings.setSalesOrderPrefix(RequestValueUtils.normalizeRequired(dto.getSalesOrderPrefix(), "Sales order prefix"));
        settings.setPurchaseOrderPrefix(RequestValueUtils.normalizeRequired(dto.getPurchaseOrderPrefix(), "Purchase order prefix"));
        Integer nextNumber = dto.getNextInvoiceNumber() != null ? dto.getNextInvoiceNumber() : 1;
        if (nextNumber < 1) {
            throw new BadRequestException("Next invoice number must be positive");
        }
        settings.setNextInvoiceNumber(nextNumber);
        settings.setInvoiceFooterText(RequestValueUtils.normalize(dto.getInvoiceFooterText()));
        settings.setDefaultPaymentTerms(RequestValueUtils.normalize(dto.getDefaultPaymentTerms()));
        return toDto(repository.save(settings));
    }

    private InvoiceSettings getOrCreate() {
        return repository.findById(SETTINGS_ID).orElseGet(() -> {
            InvoiceSettings settings = new InvoiceSettings();
            settings.setId(SETTINGS_ID);
            settings.setSalesInvoicePrefix("INV");
            settings.setPurchaseInvoicePrefix("PINV");
            settings.setSalesOrderPrefix("SO");
            settings.setPurchaseOrderPrefix("PO");
            settings.setNextInvoiceNumber(1);
            settings.setInvoiceFooterText("Thank you for your business.");
            settings.setDefaultPaymentTerms("Due on receipt");
            return repository.save(settings);
        });
    }

    private InvoiceSettingsDTO toDto(InvoiceSettings settings) {
        InvoiceSettingsDTO dto = new InvoiceSettingsDTO();
        dto.setId(settings.getId());
        dto.setSalesInvoicePrefix(settings.getSalesInvoicePrefix());
        dto.setPurchaseInvoicePrefix(settings.getPurchaseInvoicePrefix());
        dto.setSalesOrderPrefix(settings.getSalesOrderPrefix());
        dto.setPurchaseOrderPrefix(settings.getPurchaseOrderPrefix());
        dto.setNextInvoiceNumber(settings.getNextInvoiceNumber());
        dto.setInvoiceFooterText(settings.getInvoiceFooterText());
        dto.setDefaultPaymentTerms(settings.getDefaultPaymentTerms());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }
}
