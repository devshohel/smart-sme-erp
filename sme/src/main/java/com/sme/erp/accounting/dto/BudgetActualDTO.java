package com.sme.erp.accounting.dto; import java.math.BigDecimal; import java.util.List;
public record BudgetActualDTO(List<BudgetActualRowDTO> rows,BigDecimal totalBudget,BigDecimal totalActual,BigDecimal totalVariance,BigDecimal utilizationPercentage){}
