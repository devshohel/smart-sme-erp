package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.util.List;

public record BalanceSheetDTO(List<FinancialStatementLineDTO> assets,
                              List<FinancialStatementLineDTO> liabilities,
                              List<FinancialStatementLineDTO> equity,
                              BigDecimal totalAssets, BigDecimal totalLiabilities,
                              BigDecimal ownerCapital, BigDecimal retainedEarnings,
                              BigDecimal currentProfitLoss, BigDecimal totalEquity,
                              BigDecimal liabilitiesAndEquity, boolean outOfBalance,
                              BigDecimal differenceAmount) {}
