package com.sme.erp.settings.service;

import com.sme.erp.settings.dto.SystemSettingsDTO;
import com.sme.erp.settings.entity.SystemSettings;
import com.sme.erp.settings.repository.SystemSettingsRepository;
import com.sme.erp.settings.service.impl.SystemSettingsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemSettingsServiceImplTest {

    @Mock private SystemSettingsRepository repository;

    @Test
    void update_roundTripsControlledSalesFlags() {
        SystemSettings existing = new SystemSettings();
        existing.setId(1L);
        existing.setDefaultCurrency("BDT");
        existing.setDateFormat("yyyy-MM-dd");
        existing.setNumberFormat("#,##0.00");

        SystemSettingsDTO request = new SystemSettingsDTO();
        request.setDefaultCurrency("BDT");
        request.setDateFormat("yyyy-MM-dd");
        request.setNumberFormat("#,##0.00");
        request.setEnableControlledSalesMode(true);
        request.setEnableSalesOrders(true);
        request.setEnableQuotations(true);
        request.setEnableDeliveryNotes(true);
        request.setEnableSalesApproval(true);
        request.setEnableManualAllocation(true);
        request.setEnableAdvancedInvoice(true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(SystemSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSettingsDTO result = new SystemSettingsServiceImpl(repository).update(request);

        assertThat(result.getEnableControlledSalesMode()).isTrue();
        assertThat(result.getEnableSalesOrders()).isTrue();
        assertThat(result.getEnableQuotations()).isTrue();
        assertThat(result.getEnableDeliveryNotes()).isTrue();
        assertThat(result.getEnableSalesApproval()).isTrue();
        assertThat(result.getEnableManualAllocation()).isTrue();
        assertThat(result.getEnableAdvancedInvoice()).isTrue();
    }

    @Test
    void get_defaultsMissingControlledSalesFlagsToFalse() {
        SystemSettings existing = new SystemSettings();
        existing.setId(1L);
        existing.setDefaultCurrency("BDT");
        existing.setDateFormat("yyyy-MM-dd");
        existing.setNumberFormat("#,##0.00");
        existing.setEnableControlledSalesMode(null);
        existing.setEnableSalesOrders(null);
        existing.setEnableQuotations(null);
        existing.setEnableDeliveryNotes(null);
        existing.setEnableSalesApproval(null);
        existing.setEnableManualAllocation(null);
        existing.setEnableAdvancedInvoice(null);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        SystemSettingsDTO result = new SystemSettingsServiceImpl(repository).get();

        assertThat(result.getEnableControlledSalesMode()).isFalse();
        assertThat(result.getEnableSalesOrders()).isFalse();
        assertThat(result.getEnableQuotations()).isFalse();
        assertThat(result.getEnableDeliveryNotes()).isFalse();
        assertThat(result.getEnableSalesApproval()).isFalse();
        assertThat(result.getEnableManualAllocation()).isFalse();
        assertThat(result.getEnableAdvancedInvoice()).isFalse();
    }
}
