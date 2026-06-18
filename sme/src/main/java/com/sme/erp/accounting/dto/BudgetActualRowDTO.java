package com.sme.erp.accounting.dto; import java.math.BigDecimal;
public record BudgetActualRowDTO(Long accountId,String accountCode,String accountName,Long costCenterId,String costCenterCode,String costCenterName,BigDecimal budgetAmount,BigDecimal actualAmount,BigDecimal variance,BigDecimal variancePercentage){}
