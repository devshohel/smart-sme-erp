package com.sme.erp.sales.enums;

public enum SalesInvoiceStatus {
    DRAFT,
    POSTED,
    CANCELLED,
    RETURNED,

    // Legacy values kept so existing databases can still be read before V14 is applied.
    SUBMITTED,
    APPROVED,
    PARTIAL_PAID,
    PAID,
    REVERSED,
    PENDING,
    CONFIRMED,
    COMPLETED
}
