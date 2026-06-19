package com.sme.erp.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDTO {

    private String period = "month";
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime generatedAt;

    private BigDecimal periodSales = BigDecimal.ZERO;
    private BigDecimal periodPurchase = BigDecimal.ZERO;
    private BigDecimal periodExpense = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private BigDecimal totalStockValue = BigDecimal.ZERO;
    private BigDecimal customerReceivable = BigDecimal.ZERO;
    private BigDecimal supplierPayable = BigDecimal.ZERO;
    private BigDecimal cashBankBalance = BigDecimal.ZERO;
    private long lowStockItemsCount;
    private long pendingApprovalsCount;
    private BigDecimal trialBalanceDifference = BigDecimal.ZERO;
    private BigDecimal budgetUtilization = BigDecimal.ZERO;

    private BigDecimal todaySales = BigDecimal.ZERO;
    private BigDecimal todayPurchase = BigDecimal.ZERO;
    private BigDecimal todayExpense = BigDecimal.ZERO;
    private BigDecimal todayProfit = BigDecimal.ZERO;
    private BigDecimal customerDue = BigDecimal.ZERO;
    private BigDecimal supplierDue = BigDecimal.ZERO;
    private long pendingApprovalExpenses;

    private List<ChartPointDTO> monthlyIncomeExpense = new ArrayList<>();
    private List<ChartPointDTO> expenseByCategory = new ArrayList<>();
    private List<ChartPointDTO> warehouseStockValue = new ArrayList<>();
    private List<ChartPointDTO> cashBankTrend = new ArrayList<>();
    private List<MonthlySalesPurchaseDTO> salesPurchaseTrend = new ArrayList<>();
    private List<MonthlySalesPurchaseDTO> monthlySalesPurchase = new ArrayList<>();
    private List<TopSellingProductDTO> topSellingProducts = new ArrayList<>();
    private List<LowStockAlertDTO> lowStockAlerts = new ArrayList<>();
    private List<DueAlertDTO> dueAlerts = new ArrayList<>();
    private List<RecentTransactionDTO> recentTransactions = new ArrayList<>();
    private List<PendingApprovalDTO> pendingApprovals = new ArrayList<>();
    private List<RecentDocumentDTO> recentSales = new ArrayList<>();
    private List<RecentDocumentDTO> recentPurchases = new ArrayList<>();

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public BigDecimal getPeriodSales() { return periodSales; }
    public void setPeriodSales(BigDecimal periodSales) { this.periodSales = periodSales; }

    public BigDecimal getPeriodPurchase() { return periodPurchase; }
    public void setPeriodPurchase(BigDecimal periodPurchase) { this.periodPurchase = periodPurchase; }

    public BigDecimal getPeriodExpense() { return periodExpense; }
    public void setPeriodExpense(BigDecimal periodExpense) { this.periodExpense = periodExpense; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }

    public BigDecimal getCustomerReceivable() { return customerReceivable; }
    public void setCustomerReceivable(BigDecimal customerReceivable) { this.customerReceivable = customerReceivable; }

    public BigDecimal getSupplierPayable() { return supplierPayable; }
    public void setSupplierPayable(BigDecimal supplierPayable) { this.supplierPayable = supplierPayable; }

    public BigDecimal getCashBankBalance() { return cashBankBalance; }
    public void setCashBankBalance(BigDecimal cashBankBalance) { this.cashBankBalance = cashBankBalance; }

    public long getLowStockItemsCount() { return lowStockItemsCount; }
    public void setLowStockItemsCount(long lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }

    public long getPendingApprovalsCount() { return pendingApprovalsCount; }
    public void setPendingApprovalsCount(long pendingApprovalsCount) { this.pendingApprovalsCount = pendingApprovalsCount; }

    public BigDecimal getTrialBalanceDifference() { return trialBalanceDifference; }
    public void setTrialBalanceDifference(BigDecimal trialBalanceDifference) { this.trialBalanceDifference = trialBalanceDifference; }

    public BigDecimal getBudgetUtilization() { return budgetUtilization; }
    public void setBudgetUtilization(BigDecimal budgetUtilization) { this.budgetUtilization = budgetUtilization; }

    public BigDecimal getTodaySales() { return todaySales; }
    public void setTodaySales(BigDecimal todaySales) { this.todaySales = todaySales; }

    public BigDecimal getTodayPurchase() { return todayPurchase; }
    public void setTodayPurchase(BigDecimal todayPurchase) { this.todayPurchase = todayPurchase; }

    public BigDecimal getTodayExpense() { return todayExpense; }
    public void setTodayExpense(BigDecimal todayExpense) { this.todayExpense = todayExpense; }

    public BigDecimal getTodayProfit() { return todayProfit; }
    public void setTodayProfit(BigDecimal todayProfit) { this.todayProfit = todayProfit; }

    public BigDecimal getCustomerDue() { return customerDue; }
    public void setCustomerDue(BigDecimal customerDue) { this.customerDue = customerDue; }

    public BigDecimal getSupplierDue() { return supplierDue; }
    public void setSupplierDue(BigDecimal supplierDue) { this.supplierDue = supplierDue; }

    public long getPendingApprovalExpenses() { return pendingApprovalExpenses; }
    public void setPendingApprovalExpenses(long pendingApprovalExpenses) { this.pendingApprovalExpenses = pendingApprovalExpenses; }

    public List<ChartPointDTO> getMonthlyIncomeExpense() { return monthlyIncomeExpense; }
    public void setMonthlyIncomeExpense(List<ChartPointDTO> monthlyIncomeExpense) { this.monthlyIncomeExpense = monthlyIncomeExpense; }

    public List<ChartPointDTO> getExpenseByCategory() { return expenseByCategory; }
    public void setExpenseByCategory(List<ChartPointDTO> expenseByCategory) { this.expenseByCategory = expenseByCategory; }

    public List<ChartPointDTO> getWarehouseStockValue() { return warehouseStockValue; }
    public void setWarehouseStockValue(List<ChartPointDTO> warehouseStockValue) { this.warehouseStockValue = warehouseStockValue; }

    public List<ChartPointDTO> getCashBankTrend() { return cashBankTrend; }
    public void setCashBankTrend(List<ChartPointDTO> cashBankTrend) { this.cashBankTrend = cashBankTrend; }

    public List<MonthlySalesPurchaseDTO> getSalesPurchaseTrend() { return salesPurchaseTrend; }
    public void setSalesPurchaseTrend(List<MonthlySalesPurchaseDTO> salesPurchaseTrend) { this.salesPurchaseTrend = salesPurchaseTrend; }

    public List<MonthlySalesPurchaseDTO> getMonthlySalesPurchase() { return monthlySalesPurchase; }
    public void setMonthlySalesPurchase(List<MonthlySalesPurchaseDTO> monthlySalesPurchase) { this.monthlySalesPurchase = monthlySalesPurchase; }

    public List<TopSellingProductDTO> getTopSellingProducts() { return topSellingProducts; }
    public void setTopSellingProducts(List<TopSellingProductDTO> topSellingProducts) { this.topSellingProducts = topSellingProducts; }

    public List<LowStockAlertDTO> getLowStockAlerts() { return lowStockAlerts; }
    public void setLowStockAlerts(List<LowStockAlertDTO> lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }

    public List<DueAlertDTO> getDueAlerts() { return dueAlerts; }
    public void setDueAlerts(List<DueAlertDTO> dueAlerts) { this.dueAlerts = dueAlerts; }

    public List<RecentTransactionDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<RecentTransactionDTO> recentTransactions) { this.recentTransactions = recentTransactions; }

    public List<PendingApprovalDTO> getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(List<PendingApprovalDTO> pendingApprovals) { this.pendingApprovals = pendingApprovals; }

    public List<RecentDocumentDTO> getRecentSales() { return recentSales; }
    public void setRecentSales(List<RecentDocumentDTO> recentSales) { this.recentSales = recentSales; }

    public List<RecentDocumentDTO> getRecentPurchases() { return recentPurchases; }
    public void setRecentPurchases(List<RecentDocumentDTO> recentPurchases) { this.recentPurchases = recentPurchases; }

    public static class ChartPointDTO {
        private String label;
        private BigDecimal value = BigDecimal.ZERO;
        private BigDecimal secondaryValue = BigDecimal.ZERO;

        public ChartPointDTO() {}

        public ChartPointDTO(String label, BigDecimal value, BigDecimal secondaryValue) {
            this.label = label;
            this.value = value;
            this.secondaryValue = secondaryValue;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public BigDecimal getSecondaryValue() { return secondaryValue; }
        public void setSecondaryValue(BigDecimal secondaryValue) { this.secondaryValue = secondaryValue; }
    }

    public static class MonthlySalesPurchaseDTO {
        private String label;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal purchase = BigDecimal.ZERO;

        public MonthlySalesPurchaseDTO() {}

        public MonthlySalesPurchaseDTO(String label, BigDecimal sales, BigDecimal purchase) {
            this.label = label;
            this.sales = sales;
            this.purchase = purchase;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getSales() { return sales; }
        public void setSales(BigDecimal sales) { this.sales = sales; }
        public BigDecimal getPurchase() { return purchase; }
        public void setPurchase(BigDecimal purchase) { this.purchase = purchase; }
    }

    public static class TopSellingProductDTO {
        private Long productId;
        private String productName;
        private String sku;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;

        public TopSellingProductDTO() {}

        public TopSellingProductDTO(Long productId, String productName, String sku, BigDecimal quantity, BigDecimal amount) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.quantity = quantity;
            this.amount = amount;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class LowStockAlertDTO {
        private Long productId;
        private String productName;
        private String sku;
        private String warehouseName;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal reorderLevel = BigDecimal.ZERO;

        public LowStockAlertDTO() {}

        public LowStockAlertDTO(Long productId, String productName, String sku, String warehouseName,
                                BigDecimal quantity, BigDecimal reorderLevel) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.warehouseName = warehouseName;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }
    }

    public static class DueAlertDTO {
        private String type;
        private String referenceNo;
        private String partyName;
        private BigDecimal dueAmount = BigDecimal.ZERO;
        private String date;

        public DueAlertDTO() {}

        public DueAlertDTO(String type, String referenceNo, String partyName, BigDecimal dueAmount, String date) {
            this.type = type;
            this.referenceNo = referenceNo;
            this.partyName = partyName;
            this.dueAmount = dueAmount;
            this.date = date;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getReferenceNo() { return referenceNo; }
        public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
        public String getPartyName() { return partyName; }
        public void setPartyName(String partyName) { this.partyName = partyName; }
        public BigDecimal getDueAmount() { return dueAmount; }
        public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class RecentTransactionDTO {
        private String type;
        private String referenceNo;
        private String partyName;
        private String description;
        private BigDecimal amount = BigDecimal.ZERO;
        private String status;
        private String date;

        public RecentTransactionDTO() {}

        public RecentTransactionDTO(String type, String referenceNo, String partyName, String description,
                                    BigDecimal amount, String status, String date) {
            this.type = type;
            this.referenceNo = referenceNo;
            this.partyName = partyName;
            this.description = description;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getReferenceNo() { return referenceNo; }
        public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
        public String getPartyName() { return partyName; }
        public void setPartyName(String partyName) { this.partyName = partyName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class PendingApprovalDTO {
        private String type;
        private String referenceNo;
        private String partyName;
        private BigDecimal amount = BigDecimal.ZERO;
        private String status;
        private String date;

        public PendingApprovalDTO() {}

        public PendingApprovalDTO(String type, String referenceNo, String partyName,
                                  BigDecimal amount, String status, String date) {
            this.type = type;
            this.referenceNo = referenceNo;
            this.partyName = partyName;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getReferenceNo() { return referenceNo; }
        public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
        public String getPartyName() { return partyName; }
        public void setPartyName(String partyName) { this.partyName = partyName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class RecentDocumentDTO {
        private String referenceNo;
        private String partyName;
        private BigDecimal amount = BigDecimal.ZERO;
        private String status;
        private String date;

        public RecentDocumentDTO() {}

        public RecentDocumentDTO(String referenceNo, String partyName, BigDecimal amount, String status, String date) {
            this.referenceNo = referenceNo;
            this.partyName = partyName;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }

        public String getReferenceNo() { return referenceNo; }
        public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
        public String getPartyName() { return partyName; }
        public void setPartyName(String partyName) { this.partyName = partyName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }
}
