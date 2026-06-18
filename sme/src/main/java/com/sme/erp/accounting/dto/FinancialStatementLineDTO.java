package com.sme.erp.accounting.dto;

import java.math.BigDecimal;

public record FinancialStatementLineDTO(Long accountId, String accountCode, String accountName,
                                        String groupName, BigDecimal amount) {}
