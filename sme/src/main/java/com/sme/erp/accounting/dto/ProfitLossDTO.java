package com.sme.erp.accounting.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProfitLossDTO(List<FinancialStatementLineDTO> income,
                            List<FinancialStatementLineDTO> expenses,
                            BigDecimal totalIncome, BigDecimal totalExpense,
                            BigDecimal netProfitLoss, boolean outOfBalance,
                            BigDecimal differenceAmount) {}
