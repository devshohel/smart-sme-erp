package com.sme.erp.settings.dto;

public record SalesFeatureSettingsDTO(
        boolean enableControlledSalesMode,
        boolean enableSalesOrders,
        boolean enableQuotations,
        boolean enableDeliveryNotes,
        boolean enableSalesApproval,
        boolean enableManualAllocation,
        boolean enableAdvancedInvoice) {

    public static SalesFeatureSettingsDTO from(SystemSettingsDTO settings) {
        return new SalesFeatureSettingsDTO(
                Boolean.TRUE.equals(settings.getEnableControlledSalesMode()),
                Boolean.TRUE.equals(settings.getEnableSalesOrders()),
                Boolean.TRUE.equals(settings.getEnableQuotations()),
                Boolean.TRUE.equals(settings.getEnableDeliveryNotes()),
                Boolean.TRUE.equals(settings.getEnableSalesApproval()),
                Boolean.TRUE.equals(settings.getEnableManualAllocation()),
                Boolean.TRUE.equals(settings.getEnableAdvancedInvoice()));
    }
}
