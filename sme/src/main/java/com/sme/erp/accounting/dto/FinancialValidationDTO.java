package com.sme.erp.accounting.dto;

import java.math.BigDecimal;

public record FinancialValidationDTO(boolean outOfBalance, BigDecimal differenceAmount) {}
