package com.sme.erp.dashboard.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDTO {

    private BigDecimal todaySales = BigDecimal.ZERO;
    private BigDecimal todayPurchase = BigDecimal.ZERO;
    private BigDecimal todayExpense = BigDecimal.ZERO;
    private BigDecimal todayProfit = BigDecimal.ZERO;
    private BigDecimal totalStockValue = BigDecimal.ZERO;
    private BigDecimal customerDue = BigDecimal.ZERO;
    private BigDecimal supplierDue = BigDecimal.ZERO;
    private BigDecimal netProfit = BigDecimal.ZERO;
    private long totalCustomers;
    private long totalSuppliers;
    private long lowStockItemsCount;
    private BigDecimal thisMonthProfit = BigDecimal.ZERO;
    private List<MonthlySalesPurchaseDTO> monthlySalesPurchase = new ArrayList<>();
    private List<TopSellingProductDTO> topSellingProducts = new ArrayList<>();
    private List<LowStockAlertDTO> lowStockAlerts = new ArrayList<>();
    private List<DueAlertDTO> dueAlerts = new ArrayList<>();
    private List<RecentTransactionDTO> recentTransactions = new ArrayList<>();

    public BigDecimal getTodaySales() { return todaySales; }
    public void setTodaySales(BigDecimal todaySales) { this.todaySales = todaySales; }

    public BigDecimal getTodayPurchase() { return todayPurchase; }
    public void setTodayPurchase(BigDecimal todayPurchase) { this.todayPurchase = todayPurchase; }

    public BigDecimal getTodayExpense() { return todayExpense; }
    public void setTodayExpense(BigDecimal todayExpense) { this.todayExpense = todayExpense; }

    public BigDecimal getTodayProfit() { return todayProfit; }
    public void setTodayProfit(BigDecimal todayProfit) { this.todayProfit = todayProfit; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }

    public BigDecimal getCustomerDue() { return customerDue; }
    public void setCustomerDue(BigDecimal customerDue) { this.customerDue = customerDue; }

    public BigDecimal getSupplierDue() { return supplierDue; }
    public void setSupplierDue(BigDecimal supplierDue) { this.supplierDue = supplierDue; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getTotalSuppliers() { return totalSuppliers; }
    public void setTotalSuppliers(long totalSuppliers) { this.totalSuppliers = totalSuppliers; }

    public long getLowStockItemsCount() { return lowStockItemsCount; }
    public void setLowStockItemsCount(long lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }

    public BigDecimal getThisMonthProfit() { return thisMonthProfit; }
    public void setThisMonthProfit(BigDecimal thisMonthProfit) { this.thisMonthProfit = thisMonthProfit; }

    public List<MonthlySalesPurchaseDTO> getMonthlySalesPurchase() { return monthlySalesPurchase; }
    public void setMonthlySalesPurchase(List<MonthlySalesPurchaseDTO> monthlySalesPurchase) {
        this.monthlySalesPurchase = monthlySalesPurchase;
    }

    public List<TopSellingProductDTO> getTopSellingProducts() { return topSellingProducts; }
    public void setTopSellingProducts(List<TopSellingProductDTO> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public List<LowStockAlertDTO> getLowStockAlerts() { return lowStockAlerts; }
    public void setLowStockAlerts(List<LowStockAlertDTO> lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }

    public List<DueAlertDTO> getDueAlerts() { return dueAlerts; }
    public void setDueAlerts(List<DueAlertDTO> dueAlerts) { this.dueAlerts = dueAlerts; }

    public List<RecentTransactionDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<RecentTransactionDTO> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public static class MonthlySalesPurchaseDTO {
        private String month;
        private BigDecimal sales = BigDecimal.ZERO;
        private BigDecimal purchase = BigDecimal.ZERO;
        private BigDecimal profit = BigDecimal.ZERO;

        public MonthlySalesPurchaseDTO() {}

        public MonthlySalesPurchaseDTO(String month, BigDecimal sales, BigDecimal purchase, BigDecimal profit) {
            this.month = month;
            this.sales = sales;
            this.purchase = purchase;
            this.profit = profit;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public BigDecimal getSales() { return sales; }
        public void setSales(BigDecimal sales) { this.sales = sales; }

        public BigDecimal getPurchase() { return purchase; }
        public void setPurchase(BigDecimal purchase) { this.purchase = purchase; }

        public BigDecimal getProfit() { return profit; }
        public void setProfit(BigDecimal profit) { this.profit = profit; }
    }

    public static class TopSellingProductDTO {
        private Long productId;
        private String productName;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;

        public TopSellingProductDTO() {}

        public TopSellingProductDTO(Long productId, String productName, BigDecimal quantity, BigDecimal amount) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.amount = amount;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class LowStockAlertDTO {
        private Long productId;
        private String productName;
        private String warehouseName;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal reorderLevel = BigDecimal.ZERO;

        public LowStockAlertDTO() {}

        public LowStockAlertDTO(
                Long productId,
                String productName,
                String warehouseName,
                BigDecimal quantity,
                BigDecimal reorderLevel) {
            this.productId = productId;
            this.productName = productName;
            this.warehouseName = warehouseName;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

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

        public RecentTransactionDTO(
                String type,
                String referenceNo,
                String partyName,
                String description,
                BigDecimal amount,
                String status,
                String date) {
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
}
