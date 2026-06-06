package com.sme.erp.reports.dto;

import java.math.BigDecimal;

public class ProfitLossSummaryDTO {
    private BigDecimal revenue;
    private BigDecimal purchaseCost;
    private BigDecimal grossProfit;
    private BigDecimal expense;
    private BigDecimal netProfit;

    public ProfitLossSummaryDTO(BigDecimal revenue, BigDecimal purchaseCost, BigDecimal expense) {
        this.revenue = revenue;
        this.purchaseCost = purchaseCost;
        this.expense = expense;
        this.grossProfit = revenue.subtract(purchaseCost);
        this.netProfit = revenue.subtract(purchaseCost).subtract(expense);
    }

    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getPurchaseCost() { return purchaseCost; }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public BigDecimal getExpense() { return expense; }
    public BigDecimal getNetProfit() { return netProfit; }
}
