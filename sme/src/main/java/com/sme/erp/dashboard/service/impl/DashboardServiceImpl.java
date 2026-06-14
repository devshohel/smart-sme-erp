package com.sme.erp.dashboard.service.impl;

import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.DueAlertDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.LowStockAlertDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.MonthlySalesPurchaseDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.RecentTransactionDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.TopSellingProductDTO;
import com.sme.erp.dashboard.service.DashboardService;
import com.sme.erp.inventory.entity.Stock;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.enums.Status;
import com.sme.erp.product.entity.Product;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.entity.SalesItem;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import com.sme.erp.sales.repository.SalesItemRepository;
import com.sme.erp.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int DASHBOARD_MONTHS = 6;
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesItemRepository salesItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockRepository stockRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardServiceImpl(
            SalesInvoiceRepository salesInvoiceRepository,
            SalesItemRepository salesItemRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockRepository stockRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            ExpenseRepository expenseRepository) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesItemRepository = salesItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockRepository = stockRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        List<SalesInvoice> salesInvoices = salesInvoiceRepository.findAll();
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();
        List<Stock> stocks = stockRepository.findAll();

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        BigDecimal todayExpense = safe(expenseRepository.sumActiveAmountBetween(today, today));
        BigDecimal totalExpense = safe(expenseRepository.sumActiveAmountBetween(null, null));

        BigDecimal todaySales = salesInvoices.stream()
                .filter(this::isCompletedSale)
                .filter(invoice -> isSameDate(invoice.getSaleDate().toLocalDate(), today))
                .map(SalesInvoice::getNetTotal)
                .reduce(BigDecimal.ZERO, this::add);

        BigDecimal todayPurchase = purchaseOrders.stream()
                .filter(this::isActivePurchase)
                .filter(order -> isSameDate(order.getPurchaseDate().toLocalDate(), today))
                .map(PurchaseOrder::getNetTotal)
                .reduce(BigDecimal.ZERO, this::add);

        BigDecimal totalSales = salesInvoices.stream()
                .filter(this::isCompletedSale)
                .map(SalesInvoice::getNetTotal)
                .reduce(BigDecimal.ZERO, this::add);

        BigDecimal totalPurchase = purchaseOrders.stream()
                .filter(this::isActivePurchase)
                .map(PurchaseOrder::getNetTotal)
                .reduce(BigDecimal.ZERO, this::add);

        BigDecimal thisMonthSales = salesInvoices.stream()
                .filter(this::isCompletedSale)
                .filter(invoice -> YearMonth.from(invoice.getSaleDate()).equals(currentMonth))
                .map(SalesInvoice::getNetTotal)
                .reduce(BigDecimal.ZERO, this::add);

        BigDecimal thisMonthPurchase = purchaseOrders.stream()
                .filter(this::isActivePurchase)
                .filter(order -> YearMonth.from(order.getPurchaseDate()).equals(currentMonth))
                .map(PurchaseOrder::getNetTotal)
                .reduce(BigDecimal.ZERO, this::add);

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTodaySales(todaySales);
        summary.setTodayPurchase(todayPurchase);
        summary.setTodayExpense(todayExpense);
        summary.setTodayProfit(todaySales.subtract(todayPurchase).subtract(todayExpense));
        summary.setTotalStockValue(calculateTotalStockValue(stocks));
        summary.setCustomerDue(calculateCustomerDue(salesInvoices));
        summary.setSupplierDue(calculateSupplierDue(purchaseOrders));
        summary.setNetProfit(totalSales.subtract(totalPurchase).subtract(totalExpense));
        summary.setTotalCustomers(customerRepository.count());
        summary.setTotalSuppliers(supplierRepository.count());
        summary.setLowStockAlerts(buildLowStockAlerts(stocks));
        summary.setLowStockItemsCount(summary.getLowStockAlerts().size());
        BigDecimal thisMonthExpense = safe(expenseRepository.sumActiveAmountBetween(currentMonth.atDay(1), currentMonth.atEndOfMonth()));
        summary.setThisMonthProfit(thisMonthSales.subtract(thisMonthPurchase).subtract(thisMonthExpense));
        summary.setMonthlySalesPurchase(buildMonthlySalesPurchase(salesInvoices, purchaseOrders));
        summary.setTopSellingProducts(buildTopSellingProducts());
        summary.setDueAlerts(buildDueAlerts(salesInvoices, purchaseOrders));
        summary.setRecentTransactions(buildRecentTransactions(salesInvoices, purchaseOrders, expenses));

        return summary;
    }

    private BigDecimal calculateTotalStockValue(List<Stock> stocks) {
        return stocks.stream()
                .map(stock -> safe(stock.getQuantity()).multiply(productPurchasePrice(stock.getProduct())))
                .reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal calculateCustomerDue(List<SalesInvoice> invoices) {
        return invoices.stream()
                .filter(this::isCompletedSale)
                .map(SalesInvoice::getDueAmount)
                .filter(this::isPositive)
                .reduce(BigDecimal.ZERO, this::add);
    }

    private BigDecimal calculateSupplierDue(List<PurchaseOrder> orders) {
        return orders.stream()
                .filter(this::isActivePurchase)
                .map(PurchaseOrder::getDueAmount)
                .filter(this::isPositive)
                .reduce(BigDecimal.ZERO, this::add);
    }

    private List<LowStockAlertDTO> buildLowStockAlerts(List<Stock> stocks) {
        return stocks.stream()
                .filter(this::isLowStock)
                .sorted(Comparator.comparing(Stock::getQuantity))
                .limit(8)
                .map(stock -> new LowStockAlertDTO(
                        stock.getProduct().getId(),
                        stock.getProduct().getProductName(),
                        stock.getWarehouse().getName(),
                        safe(stock.getQuantity()),
                        reorderLevel(stock)))
                .collect(Collectors.toList());
    }

    private List<MonthlySalesPurchaseDTO> buildMonthlySalesPurchase(
            List<SalesInvoice> salesInvoices,
            List<PurchaseOrder> purchaseOrders) {
        List<MonthlySalesPurchaseDTO> rows = new ArrayList<>();
        YearMonth start = YearMonth.now().minusMonths(DASHBOARD_MONTHS - 1L);

        for (int i = 0; i < DASHBOARD_MONTHS; i++) {
            YearMonth month = start.plusMonths(i);
            BigDecimal sales = salesInvoices.stream()
                    .filter(this::isCompletedSale)
                    .filter(invoice -> YearMonth.from(invoice.getSaleDate()).equals(month))
                    .map(SalesInvoice::getNetTotal)
                    .reduce(BigDecimal.ZERO, this::add);

            BigDecimal purchase = purchaseOrders.stream()
                    .filter(this::isActivePurchase)
                    .filter(order -> YearMonth.from(order.getPurchaseDate()).equals(month))
                    .map(PurchaseOrder::getNetTotal)
                    .reduce(BigDecimal.ZERO, this::add);

            rows.add(new MonthlySalesPurchaseDTO(
                    month.format(MONTH_LABEL_FORMAT),
                    sales,
                    purchase,
                    sales.subtract(purchase)));
        }

        return rows;
    }

    private List<TopSellingProductDTO> buildTopSellingProducts() {
        Map<Long, ProductSalesTotal> totals = new LinkedHashMap<>();

        for (SalesItem item : salesItemRepository.findAll()) {
            SalesInvoice invoice = item.getInvoice();
            Product product = item.getProduct();
            if (invoice == null || product == null || !isCompletedSale(invoice)) {
                continue;
            }

            ProductSalesTotal total = totals.computeIfAbsent(
                    product.getId(),
                    id -> new ProductSalesTotal(product.getId(), product.getProductName()));
            total.add(safe(item.getQuantity()), safe(item.getSubTotal()));
        }

        return totals.values().stream()
                .sorted(Comparator.comparing(ProductSalesTotal::amount).reversed())
                .limit(5)
                .map(total -> new TopSellingProductDTO(
                        total.productId(),
                        total.productName(),
                        total.quantity(),
                        total.amount()))
                .collect(Collectors.toList());
    }

    private List<DueAlertDTO> buildDueAlerts(List<SalesInvoice> salesInvoices, List<PurchaseOrder> purchaseOrders) {
        List<DueAlertDTO> alerts = new ArrayList<>();

        salesInvoices.stream()
                .filter(this::isCompletedSale)
                .filter(invoice -> isPositive(invoice.getDueAmount()))
                .forEach(invoice -> alerts.add(new DueAlertDTO(
                        "Customer Due",
                        invoice.getInvoiceNo(),
                        invoice.getCustomer().getName(),
                        safe(invoice.getDueAmount()),
                        invoice.getSaleDate().toLocalDate().toString())));

        purchaseOrders.stream()
                .filter(this::isActivePurchase)
                .filter(order -> isPositive(order.getDueAmount()))
                .forEach(order -> alerts.add(new DueAlertDTO(
                        "Supplier Due",
                        order.getPurchaseCode(),
                        order.getSupplier().getName(),
                        safe(order.getDueAmount()),
                        order.getPurchaseDate().toLocalDate().toString())));

        return alerts.stream()
                .sorted(Comparator.comparing(DueAlertDTO::getDueAmount).reversed())
                .limit(8)
                .collect(Collectors.toList());
    }

    private List<RecentTransactionDTO> buildRecentTransactions(
            List<SalesInvoice> salesInvoices,
            List<PurchaseOrder> purchaseOrders,
            List<Expense> expenses) {
        List<RecentTransactionDTO> transactions = new ArrayList<>();

        salesInvoices.stream()
                .filter(this::isCompletedSale)
                .forEach(invoice -> transactions.add(new RecentTransactionDTO(
                        "Sales Invoice",
                        invoice.getInvoiceNo(),
                        invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Customer",
                        "Sales invoice",
                        safe(invoice.getNetTotal()),
                        invoice.getPaymentStatus().name(),
                        invoice.getSaleDate().toString())));

        purchaseOrders.stream()
                .filter(this::isActivePurchase)
                .forEach(order -> transactions.add(new RecentTransactionDTO(
                        "Purchase",
                        order.getPurchaseCode(),
                        order.getSupplier() != null ? order.getSupplier().getName() : "Supplier",
                        order.getWarehouse() != null ? order.getWarehouse().getName() : "Purchase",
                        safe(order.getNetTotal()),
                        order.getStatus().name(),
                        order.getPurchaseDate() != null
                                ? order.getPurchaseDate().toString()
                                : order.getCreatedAt().toString())));

        expenses.stream()
                .filter(expense -> expense != null && (expense.getExpenseDate() != null || expense.getCreatedAt() != null))
                .forEach(expense -> transactions.add(new RecentTransactionDTO(
                        "Expense",
                        expense.getExpenseNo() != null && !expense.getExpenseNo().isBlank()
                                ? expense.getExpenseNo()
                                : "EXP-" + expense.getId(),
                        expense.getCategory() != null ? expense.getCategory().getName() : "Expense",
                        expense.getNotes() != null && !expense.getNotes().isBlank()
                                ? expense.getNotes()
                                : expense.getCategory() != null ? expense.getCategory().getName() : "Expense",
                        safe(expense.getAmount()),
                        expense.getStatus() != null ? expense.getStatus().name() : "ACTIVE",
                        expense.getExpenseDate() != null
                                ? expense.getExpenseDate().toString()
                                : expense.getCreatedAt().toLocalDate().toString())));

        return transactions.stream()
                .sorted(Comparator.comparing(RecentTransactionDTO::getDate).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean isCompletedSale(SalesInvoice invoice) {
        return invoice != null
                && invoice.getSaleDate() != null
                && (invoice.getStatus() == SalesInvoiceStatus.CONFIRMED
                || invoice.getStatus() == SalesInvoiceStatus.COMPLETED);
    }

    private boolean isActivePurchase(PurchaseOrder order) {
        return order != null
                && order.getPurchaseDate() != null
                && (order.getStatus() == PurchaseStatus.RECEIVED
                || order.getStatus() == PurchaseStatus.PARTIAL_PAID
                || order.getStatus() == PurchaseStatus.PAID);
    }

    private boolean isLowStock(Stock stock) {
        if (stock == null || !isActiveProduct(stock.getProduct())) {
            return false;
        }
        BigDecimal reorderLevel = reorderLevel(stock);
        return reorderLevel.signum() > 0 && safe(stock.getQuantity()).compareTo(reorderLevel) <= 0;
    }

    private boolean isActiveProduct(Product product) {
        return product != null
                && product.getStatus() == Status.ACTIVE
                && !Boolean.TRUE.equals(product.getDeleted());
    }

    private BigDecimal reorderLevel(Stock stock) {
        if (stock.getProduct() != null && stock.getProduct().getReorderLevel() != null) {
            return BigDecimal.valueOf(stock.getProduct().getReorderLevel());
        }
        if (stock.getReorderLevel() != null) {
            return BigDecimal.valueOf(stock.getReorderLevel());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal productPurchasePrice(Product product) {
        return product == null ? BigDecimal.ZERO : safe(product.getPurchasePrice());
    }

    private boolean isPositive(BigDecimal value) {
        return safe(value).signum() > 0;
    }

    private boolean isSameDate(LocalDate left, LocalDate right) {
        return left != null && left.equals(right);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal add(BigDecimal left, BigDecimal right) {
        return safe(left).add(safe(right));
    }

    private static class ProductSalesTotal {
        private final Long productId;
        private final String productName;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal amount = BigDecimal.ZERO;

        ProductSalesTotal(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        void add(BigDecimal quantity, BigDecimal amount) {
            this.quantity = this.quantity.add(quantity);
            this.amount = this.amount.add(amount);
        }

        Long productId() { return productId; }
        String productName() { return productName; }
        BigDecimal quantity() { return quantity; }
        BigDecimal amount() { return amount; }
    }
}
