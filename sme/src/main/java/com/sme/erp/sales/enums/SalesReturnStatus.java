package com.sme.erp.sales.enums;

public enum SalesReturnStatus {
    PENDING,
    APPROVED,
    REJECTED,

    // Legacy values kept so the application can read pre-migration rows safely.
    DRAFT,
    SUBMITTED,
    POSTED,
    REVERSED,
    CANCELLED
}
