package com.sme.erp.dashboard.service.impl;

import com.sme.erp.accounting.dto.BudgetActualDTO;
import com.sme.erp.accounting.dto.ProfitLossDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.accounting.service.AccountingReportService;
import com.sme.erp.accounting.service.BudgetService;
import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.ChartPointDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.DueAlertDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.LowStockAlertDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.MonthlySalesPurchaseDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.PendingApprovalDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.RecentDocumentDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.RecentTransactionDTO;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO.TopSellingProductDTO;
import com.sme.erp.dashboard.service.DashboardService;
import com.sme.erp.inventory.entity.StockTransfer;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.inventory.repository.StockTransferRepository;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.CustomerSalesRowDTO;
import com.sme.erp.reports.dto.LowStockReportDTO;
import com.sme.erp.reports.dto.LowStockRowDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.reports.dto.SupplierPurchaseRowDTO;
import com.sme.erp.reports.dto.TopSellingProductReportDTO;
import com.sme.erp.reports.dto.TopSellingProductRowDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationReportDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationRowDTO;
import com.sme.erp.reports.service.ReportService;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int LIST_LIMIT = 6;
    private static final int CHART_LIMIT = 5;
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final ReportService reportService;
    private final AccountingReportService accountingReportService;
    private final BudgetService budgetService;
    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final ExpenseRepository expenseRepository;
    private final StockRepository stockRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockTransferRepository stockTransferRepository;

    public DashboardServiceImpl(
            ReportService reportService,
            AccountingReportService accountingReportService,
            BudgetService budgetService,
            AccountRepository accountRepository,
            JournalEntryLineRepository journalEntryLineRepository,
            ExpenseRepository expenseRepository,
            StockRepository stockRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockTransferRepository stockTransferRepository) {
        this.reportService = reportService;
        this.accountingReportService = accountingReportService;
        this.budgetService = budgetService;
        this.accountRepository = accountRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.expenseRepository = expenseRepository;
        this.stockRepository = stockRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockTransferRepository = stockTransferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        return getSummary("month", null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(String period, LocalDate fromDate, LocalDate toDate) {
        DateRange selectedRange = resolveRange(period, fromDate, toDate);
        DateRange todayRange = resolveRange("today", null, null);

        SalesReportDTO salesSummary = reportService.getSalesSummary(selectedRange.from(), selectedRange.to(), null);
        PurchaseReportDTO purchaseSummary = reportService.getPurchaseSummary(selectedRange.from(), selectedRange.to(), null);
        CustomerDueReportDTO customerDueReport = reportService.getCustomerDueReport();
        SupplierDueReportDTO supplierDueReport = reportService.getSupplierDueReport();
        LowStockReportDTO lowStockReport = reportService.getLowStockReport(null, null, null, null, null);
        WarehouseStockValuationReportDTO warehouseStockReport = reportService.getWarehouseStockValuation(null, null, null, null);
        TopSellingProductReportDTO topSellingProductReport = reportService.getTopSellingProducts(
                selectedRange.from(), selectedRange.to(), null, null, null, null, null);
        ProfitLossDTO profitLoss = accountingReportService.getProfitLoss(selectedRange.from(), selectedRange.to());
        TrialBalanceDTO trialBalance = accountingReportService.getTrialBalance(selectedRange.from(), selectedRange.to());
        BudgetActualDTO budgetActual = budgetService.actual(null, selectedRange.from(), selectedRange.to(), null, null);

        SalesReportDTO todaySalesSummary = reportService.getSalesSummary(todayRange.from(), todayRange.to(), null);
        PurchaseReportDTO todayPurchaseSummary = reportService.getPurchaseSummary(todayRange.from(), todayRange.to(), null);
        BigDecimal todayExpense = safe(expenseRepository.sumActiveAmountBetween(todayRange.from(), todayRange.to()));

        List<SalesReportRowDTO> salesRows = reportService.getSalesDetail(
                selectedRange.from(), selectedRange.to(), null, null, null, null).getRows();
        List<PurchaseReportRowDTO> purchaseRows = reportService.getPurchaseDetail(
                selectedRange.from(), selectedRange.to(), null, null, null).getRows();
        DateRange chartRange = resolveChartRange(selectedRange);
        List<SalesReportRowDTO> chartSalesRows = reportService.getSalesDetail(
                chartRange.from(), chartRange.to(), null, null, null, null).getRows();
        List<PurchaseReportRowDTO> chartPurchaseRows = reportService.getPurchaseDetail(
                chartRange.from(), chartRange.to(), null, null, null).getRows();
        List<Expense> postedExpenses = expenseRepository.search(selectedRange.from(), selectedRange.to(), null, null).stream()
                .filter(expense -> expense.getStatus() == ExpenseStatus.POSTED)
                .toList();

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setPeriod(selectedRange.period());
        summary.setFromDate(selectedRange.from());
        summary.setToDate(selectedRange.to());
        summary.setGeneratedAt(LocalDateTime.now());

        summary.setPeriodSales(safe(salesSummary.getTotalSales()));
        summary.setPeriodPurchase(safe(purchaseSummary.getTotalPurchase()));
        summary.setPeriodExpense(safe(expenseRepository.sumActiveAmountBetween(selectedRange.from(), selectedRange.to())));
        summary.setNetProfit(safe(profitLoss.netProfitLoss()));
        summary.setTotalStockValue(safe(stockRepository.sumInventoryValue()));
        summary.setCustomerReceivable(safe(customerDueReport.getTotalCustomerDue()));
        summary.setSupplierPayable(safe(supplierDueReport.getTotalSupplierDue()));
        summary.setCashBankBalance(cashBalanceAsOf(selectedRange.to()).add(bankBalanceAsOf(selectedRange.to())));
        summary.setLowStockItemsCount(lowStockReport.totalLowStockItems() == null ? 0L : lowStockReport.totalLowStockItems());
        PendingApprovalSummary pendingApprovalSummary = buildPendingApprovals();
        summary.setPendingApprovals(pendingApprovalSummary.rows());
        summary.setPendingApprovalsCount(pendingApprovalSummary.totalCount());
        summary.setTrialBalanceDifference(safe(trialBalance.getDifferenceAmount()));
        summary.setBudgetUtilization(safe(budgetActual.utilizationPercentage()));

        summary.setTodaySales(safe(todaySalesSummary.getTotalSales()));
        summary.setTodayPurchase(safe(todayPurchaseSummary.getTotalPurchase()));
        summary.setTodayExpense(todayExpense);
        summary.setTodayProfit(summary.getTodaySales().subtract(summary.getTodayPurchase()).subtract(summary.getTodayExpense()));
        summary.setCustomerDue(summary.getCustomerReceivable());
        summary.setSupplierDue(summary.getSupplierPayable());
        summary.setPendingApprovalExpenses(expenseRepository.approvalQueue(null, null, null, null, null, null).size());

        summary.setMonthlyIncomeExpense(buildIncomeExpenseChart(chartRange));
        List<MonthlySalesPurchaseDTO> salesPurchaseTrend = buildSalesPurchaseTrend(chartRange, chartSalesRows, chartPurchaseRows);
        summary.setSalesPurchaseTrend(salesPurchaseTrend);
        summary.setMonthlySalesPurchase(salesPurchaseTrend);
        summary.setTopSellingProducts(buildTopSellingProducts(topSellingProductReport));
        summary.setExpenseByCategory(buildExpenseByCategory(selectedRange));
        summary.setWarehouseStockValue(buildWarehouseStockValue(warehouseStockReport));
        summary.setCashBankTrend(buildCashBankTrend(selectedRange));
        summary.setLowStockAlerts(buildLowStockAlerts(lowStockReport));
        summary.setDueAlerts(buildDueAlerts(customerDueReport, supplierDueReport));
        summary.setRecentTransactions(buildRecentTransactions(salesRows, purchaseRows, postedExpenses));
        summary.setRecentSales(buildRecentSales(salesRows));
        summary.setRecentPurchases(buildRecentPurchases(purchaseRows));

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingProductDTO> getTopSellingProducts(String period, LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveRange(period, fromDate, toDate);
        return buildTopSellingProducts(reportService.getTopSellingProducts(range.from(), range.to(), null, null, null, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardSummaryDTO.CustomerAnalyticsDTO> getTopCustomers(String period, LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveRange(period, fromDate, toDate);
        return reportService.getCustomerSales(range.from(), range.to(), null, null).rows().stream()
                .limit(LIST_LIMIT)
                .map(this::customerAnalytics)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardSummaryDTO.SupplierAnalyticsDTO> getTopSuppliers(String period, LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveRange(period, fromDate, toDate);
        return reportService.getSupplierPurchases(range.from(), range.to(), null, null).rows().stream()
                .limit(LIST_LIMIT)
                .map(this::supplierAnalytics)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlySalesPurchaseDTO> getMonthlySalesTrend(String period, LocalDate fromDate, LocalDate toDate) {
        return getSummary(period, fromDate, toDate).getSalesPurchaseTrend();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlySalesPurchaseDTO> getMonthlyPurchaseTrend(String period, LocalDate fromDate, LocalDate toDate) {
        return getSummary(period, fromDate, toDate).getSalesPurchaseTrend();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartPointDTO> getMonthlyExpenseTrend(String period, LocalDate fromDate, LocalDate toDate) {
        return getSummary(period, fromDate, toDate).getMonthlyIncomeExpense().stream()
                .map(point -> new ChartPointDTO(point.getLabel(), point.getSecondaryValue(), BigDecimal.ZERO))
                .toList();
    }

    private PendingApprovalSummary buildPendingApprovals() {
        List<PendingApprovalDTO> rows = new ArrayList<>();
        long totalCount = 0L;

        List<Expense> pendingExpenses = expenseRepository.approvalQueue(null, null, null, null, null, null);
        totalCount += pendingExpenses.size();

        pendingExpenses.stream()
                .limit(LIST_LIMIT)
                .forEach(expense -> rows.add(new PendingApprovalDTO(
                        "Expense",
                        defaultText(expense.getExpenseNo(), "Expense"),
                        expense.getCategory() == null ? "Expense" : defaultText(expense.getCategory().getName(), "Expense"),
                        safe(expense.getAmount()),
                        expense.getStatus() == null ? "SUBMITTED" : expense.getStatus().name(),
                        asText(expense.getExpenseDate()))));

        totalCount += salesInvoiceRepository.countSubmittedForApproval();
        salesInvoiceRepository.findSubmittedForApproval(PageRequest.of(0, LIST_LIMIT)).forEach(invoice ->
                rows.add(new PendingApprovalDTO(
                        "Sales Invoice",
                        defaultText(invoice.getInvoiceNo(), "Invoice"),
                        invoice.getCustomer() == null ? "Customer" : defaultText(invoice.getCustomer().getName(), "Customer"),
                        safe(invoice.getNetTotal()),
                        invoice.getStatus() == null ? "SUBMITTED" : invoice.getStatus().name(),
                        invoice.getSaleDate() == null ? "" : invoice.getSaleDate().toLocalDate().toString())));

        totalCount += purchaseOrderRepository.countSubmittedForApproval();
        purchaseOrderRepository.findSubmittedForApproval(PageRequest.of(0, LIST_LIMIT)).forEach(order ->
                rows.add(new PendingApprovalDTO(
                        "Purchase Order",
                        defaultText(order.getPurchaseCode(), "Purchase"),
                        order.getSupplier() == null ? "Supplier" : defaultText(order.getSupplier().getName(), "Supplier"),
                        safe(order.getNetTotal()),
                        order.getStatus() == null ? "SUBMITTED" : order.getStatus().name(),
                        order.getPurchaseDate() == null ? "" : order.getPurchaseDate().toLocalDate().toString())));

        List<StockTransfer> pendingTransfers = stockTransferRepository.findAll().stream()
                .filter(transfer -> transfer.getStatus() == StockTransferStatus.PENDING)
                .sorted(Comparator.comparing(StockTransfer::getTransferDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(StockTransfer::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        totalCount += pendingTransfers.size();
        pendingTransfers.stream()
                .limit(LIST_LIMIT)
                .forEach(transfer -> rows.add(new PendingApprovalDTO(
                        "Stock Transfer",
                        defaultText(transfer.getTransferNo(), "Transfer"),
                        transfer.getToWarehouse() == null ? "Warehouse" : defaultText(transfer.getToWarehouse().getName(), "Warehouse"),
                        BigDecimal.ZERO,
                        transfer.getStatus().name(),
                        asText(transfer.getTransferDate()))));

        return new PendingApprovalSummary(rows.stream()
                .sorted(Comparator.comparing(PendingApprovalDTO::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(LIST_LIMIT)
                .collect(Collectors.toList()), totalCount);
    }

    private List<ChartPointDTO> buildIncomeExpenseChart(DateRange range) {
        Map<Long, AccountType> accountTypes = accountRepository.findAll().stream()
                .collect(Collectors.toMap(Account::getId, Account::getAccountType));
        Map<String, ChartPointTotals> totals = initTimeline(range);

        for (JournalEntryLine line : journalEntryLineRepository.findPostedLedgerLines(null, range.from(), range.to())) {
            String key = bucketKey(range, line.getJournalEntry().getJournalDate());
            if (key == null) {
                continue;
            }
            ChartPointTotals bucket = totals.computeIfAbsent(key, ignored -> new ChartPointTotals());
            AccountType type = accountTypes.get(line.getAccount().getId());
            if (type == AccountType.INCOME) {
                bucket.value = bucket.value.add(safe(line.getCredit()).subtract(safe(line.getDebit())));
            } else if (type == AccountType.EXPENSE) {
                bucket.secondary = bucket.secondary.add(safe(line.getDebit()).subtract(safe(line.getCredit())));
            }
        }

        return totals.entrySet().stream()
                .map(entry -> new ChartPointDTO(entry.getKey(), entry.getValue().value, entry.getValue().secondary))
                .limit(CHART_LIMIT)
                .collect(Collectors.toList());
    }

    private List<MonthlySalesPurchaseDTO> buildSalesPurchaseTrend(
            DateRange range,
            List<SalesReportRowDTO> salesRows,
            List<PurchaseReportRowDTO> purchaseRows) {
        Map<String, ChartPointTotals> totals = initTimeline(range);

        for (SalesReportRowDTO row : salesRows) {
            String key = bucketKey(range, row.getDate() == null ? null : row.getDate().toLocalDate());
            if (key != null) {
                totals.computeIfAbsent(key, ignored -> new ChartPointTotals()).value =
                        totals.get(key).value.add(safe(row.getAmount()));
            }
        }

        for (PurchaseReportRowDTO row : purchaseRows) {
            String key = bucketKey(range, row.getDate() == null ? null : row.getDate().toLocalDate());
            if (key != null) {
                totals.computeIfAbsent(key, ignored -> new ChartPointTotals()).secondary =
                        totals.get(key).secondary.add(safe(row.getAmount()));
            }
        }

        return totals.entrySet().stream()
                .map(entry -> new MonthlySalesPurchaseDTO(entry.getKey(), entry.getValue().value, entry.getValue().secondary))
                .limit(CHART_LIMIT)
                .collect(Collectors.toList());
    }

    private List<TopSellingProductDTO> buildTopSellingProducts(TopSellingProductReportDTO report) {
        return report.rows().stream()
                .sorted(Comparator.comparing((TopSellingProductRowDTO row) -> safe(row.netQty())).reversed())
                .limit(LIST_LIMIT)
                .map(row -> new TopSellingProductDTO(
                        row.productId(),
                        row.product(),
                        row.sku(),
                        safe(row.netQty()),
                        safe(row.grossSales())))
                .collect(Collectors.toList());
    }

    private DashboardSummaryDTO.CustomerAnalyticsDTO customerAnalytics(CustomerSalesRowDTO row) {
        return new DashboardSummaryDTO.CustomerAnalyticsDTO(
                row.customerId(),
                row.customer(),
                safe(row.totalSales()),
                safe(row.dueAmount()),
                row.invoiceCount());
    }

    private DashboardSummaryDTO.SupplierAnalyticsDTO supplierAnalytics(SupplierPurchaseRowDTO row) {
        return new DashboardSummaryDTO.SupplierAnalyticsDTO(
                row.supplierId(),
                row.supplier(),
                safe(row.totalPurchase()),
                safe(row.dueAmount()),
                row.purchaseCount());
    }

    private List<ChartPointDTO> buildExpenseByCategory(DateRange range) {
        return expenseRepository.summarizePostedByCategory(range.from(), range.to()).stream()
                .limit(CHART_LIMIT)
                .map(row -> new ChartPointDTO(
                        String.valueOf(row[0]),
                        safe((BigDecimal) row[1]),
                        BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    private List<ChartPointDTO> buildWarehouseStockValue(WarehouseStockValuationReportDTO report) {
        return report.rows().stream()
                .limit(CHART_LIMIT)
                .map(row -> new ChartPointDTO(row.warehouse(), safe(row.stockValue()), safe(row.totalQty())))
                .collect(Collectors.toList());
    }

    private List<ChartPointDTO> buildCashBankTrend(DateRange range) {
        Optional<Account> cashAccount = resolveAccount("1000", "Cash");
        Optional<Account> bankAccount = resolveAccount("1010", "Bank");
        BigDecimal openingCash = openingBalance(cashAccount, range.from());
        BigDecimal openingBank = openingBalance(bankAccount, range.from());

        Map<String, ChartPointTotals> totals = initTimeline(range);
        Map<String, BigDecimal> cashMovements = bucketedBalanceDeltas(cashAccount, range);
        Map<String, BigDecimal> bankMovements = bucketedBalanceDeltas(bankAccount, range);

        for (Map.Entry<String, ChartPointTotals> entry : totals.entrySet()) {
            openingCash = openingCash.add(cashMovements.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            openingBank = openingBank.add(bankMovements.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            entry.getValue().value = openingCash;
            entry.getValue().secondary = openingBank;
        }

        return totals.entrySet().stream()
                .map(entry -> new ChartPointDTO(entry.getKey(), entry.getValue().value, entry.getValue().secondary))
                .limit(CHART_LIMIT)
                .collect(Collectors.toList());
    }

    private List<LowStockAlertDTO> buildLowStockAlerts(LowStockReportDTO report) {
        return report.rows().stream()
                .limit(LIST_LIMIT)
                .map(row -> new LowStockAlertDTO(
                        null,
                        row.product(),
                        row.sku(),
                        row.warehouse(),
                        safe(row.currentQty()),
                        safe(row.reorderLevel())))
                .collect(Collectors.toList());
    }

    private List<DueAlertDTO> buildDueAlerts(CustomerDueReportDTO customerDueReport, SupplierDueReportDTO supplierDueReport) {
        List<DueAlertDTO> alerts = new ArrayList<>();

        customerDueReport.getRows().stream()
                .limit(3)
                .map(CustomerDueReportRowDTO::getCustomer)
                .forEach(customer -> alerts.add(new DueAlertDTO("Customer Due", "", customer, customerDue(customerDueReport, customer), "")));

        supplierDueReport.getRows().stream()
                .limit(3)
                .map(SupplierDueReportRowDTO::getSupplier)
                .forEach(supplier -> alerts.add(new DueAlertDTO("Supplier Due", "", supplier, supplierDue(supplierDueReport, supplier), "")));

        return alerts;
    }

    private BigDecimal customerDue(CustomerDueReportDTO report, String customer) {
        return report.getRows().stream()
                .filter(row -> row.getCustomer().equals(customer))
                .map(CustomerDueReportRowDTO::getDue)
                .findFirst()
                .map(this::safe)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal supplierDue(SupplierDueReportDTO report, String supplier) {
        return report.getRows().stream()
                .filter(row -> row.getSupplier().equals(supplier))
                .map(SupplierDueReportRowDTO::getDue)
                .findFirst()
                .map(this::safe)
                .orElse(BigDecimal.ZERO);
    }

    private List<RecentTransactionDTO> buildRecentTransactions(
            List<SalesReportRowDTO> salesRows,
            List<PurchaseReportRowDTO> purchaseRows,
            List<Expense> postedExpenses) {
        List<RecentTransactionDTO> rows = new ArrayList<>();

        salesRows.forEach(row -> rows.add(new RecentTransactionDTO(
                "Sales Invoice",
                row.getInvoiceNo(),
                row.getCustomer(),
                row.getWarehouse(),
                safe(row.getAmount()),
                row.getStatus(),
                row.getDate() == null ? "" : row.getDate().toLocalDate().toString())));

        purchaseRows.forEach(row -> rows.add(new RecentTransactionDTO(
                "Purchase",
                row.getPoNo(),
                row.getSupplier(),
                row.getWarehouse(),
                safe(row.getAmount()),
                row.getStatus(),
                row.getDate() == null ? "" : row.getDate().toLocalDate().toString())));

        postedExpenses.forEach(expense -> rows.add(new RecentTransactionDTO(
                "Expense",
                defaultText(expense.getExpenseNo(), "Expense"),
                expense.getCategory() == null ? "Expense" : defaultText(expense.getCategory().getName(), "Expense"),
                defaultText(expense.getNotes(), defaultText(expense.getReferenceNo(), "Posted expense")),
                safe(expense.getAmount()),
                expense.getStatus() == null ? "POSTED" : expense.getStatus().name(),
                asText(expense.getExpenseDate()))));

        return rows.stream()
                .sorted(Comparator.comparing(RecentTransactionDTO::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .collect(Collectors.toList());
    }

    private List<RecentDocumentDTO> buildRecentSales(List<SalesReportRowDTO> salesRows) {
        return salesRows.stream()
                .limit(LIST_LIMIT)
                .map(row -> new RecentDocumentDTO(
                        row.getInvoiceNo(),
                        row.getCustomer(),
                        safe(row.getAmount()),
                        row.getStatus(),
                        row.getDate() == null ? "" : row.getDate().toLocalDate().toString()))
                .collect(Collectors.toList());
    }

    private List<RecentDocumentDTO> buildRecentPurchases(List<PurchaseReportRowDTO> purchaseRows) {
        return purchaseRows.stream()
                .limit(LIST_LIMIT)
                .map(row -> new RecentDocumentDTO(
                        row.getPoNo(),
                        row.getSupplier(),
                        safe(row.getAmount()),
                        row.getStatus(),
                        row.getDate() == null ? "" : row.getDate().toLocalDate().toString()))
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> bucketedBalanceDeltas(Optional<Account> account, DateRange range) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        if (account.isEmpty()) {
            return totals;
        }
        for (JournalEntryLine line : journalEntryLineRepository.findPostedLedgerLines(account.get().getId(), range.from(), range.to())) {
            String key = bucketKey(range, line.getJournalEntry().getJournalDate());
            if (key == null) {
                continue;
            }
            totals.merge(key, safe(line.getDebit()).subtract(safe(line.getCredit())), BigDecimal::add);
        }
        return totals;
    }

    private BigDecimal cashBalanceAsOf(LocalDate toDate) {
        return balanceAsOf(resolveAccount("1000", "Cash"), toDate);
    }

    private BigDecimal bankBalanceAsOf(LocalDate toDate) {
        return balanceAsOf(resolveAccount("1010", "Bank"), toDate);
    }

    private BigDecimal balanceAsOf(Optional<Account> account, LocalDate toDate) {
        if (account.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return safe(journalEntryLineRepository.sumDebit(account.get().getId(), null, toDate))
                .subtract(safe(journalEntryLineRepository.sumCredit(account.get().getId(), null, toDate)));
    }

    private BigDecimal openingBalance(Optional<Account> account, LocalDate fromDate) {
        if (account.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return fromDate == null ? BigDecimal.ZERO : safe(journalEntryLineRepository.openingBalance(account.get().getId(), fromDate));
    }

    private Optional<Account> resolveAccount(String code, String name) {
        Optional<Account> byCode = accountRepository.findByAccountCodeIgnoreCase(code);
        return byCode.isPresent() ? byCode : accountRepository.findByAccountNameIgnoreCase(name);
    }

    private Map<String, ChartPointTotals> initTimeline(DateRange range) {
        Map<String, ChartPointTotals> timeline = new LinkedHashMap<>();
        if (range.granularity() == BucketGranularity.DAY) {
            LocalDate current = range.from();
            while (!current.isAfter(range.to())) {
                timeline.put(current.format(DAY_LABEL), new ChartPointTotals());
                current = current.plusDays(1);
            }
        } else {
            YearMonth current = YearMonth.from(range.from());
            YearMonth end = YearMonth.from(range.to());
            while (!current.isAfter(end)) {
                timeline.put(current.format(MONTH_LABEL), new ChartPointTotals());
                current = current.plusMonths(1);
            }
        }
        return timeline;
    }

    private String bucketKey(DateRange range, LocalDate date) {
        if (date == null) {
            return null;
        }
        return range.granularity() == BucketGranularity.DAY ? date.format(DAY_LABEL) : YearMonth.from(date).format(MONTH_LABEL);
    }

    private DateRange resolveRange(String requestedPeriod, LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        String period = requestedPeriod == null || requestedPeriod.isBlank()
                ? "month"
                : requestedPeriod.trim().toLowerCase(Locale.ENGLISH);
        return switch (period) {
            case "today" -> new DateRange("today", today, today, BucketGranularity.DAY);
            case "year" -> new DateRange("year", today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear()), BucketGranularity.MONTH);
            case "custom" -> {
                if (fromDate == null || toDate == null) {
                    throw new BadRequestException("Custom dashboard range requires fromDate and toDate.");
                }
                if (fromDate.isAfter(toDate)) {
                    throw new BadRequestException("fromDate cannot be after toDate.");
                }
                long days = ChronoUnit.DAYS.between(fromDate, toDate);
                yield new DateRange("custom", fromDate, toDate, days <= 31 ? BucketGranularity.DAY : BucketGranularity.MONTH);
            }
            default -> new DateRange("month", today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()), BucketGranularity.MONTH);
        };
    }

    private DateRange resolveChartRange(DateRange selectedRange) {
        return switch (selectedRange.period()) {
            case "today" -> new DateRange(
                    selectedRange.period(),
                    selectedRange.to().minusDays(CHART_LIMIT - 1L),
                    selectedRange.to(),
                    BucketGranularity.DAY);
            case "month" -> {
                YearMonth end = YearMonth.from(selectedRange.to());
                YearMonth start = end.minusMonths(CHART_LIMIT - 1L);
                yield new DateRange(selectedRange.period(), start.atDay(1), end.atEndOfMonth(), BucketGranularity.MONTH);
            }
            case "year" -> {
                YearMonth start = YearMonth.from(selectedRange.from());
                YearMonth end = start.plusMonths(CHART_LIMIT - 1L);
                yield new DateRange(selectedRange.period(), start.atDay(1), end.atEndOfMonth(), BucketGranularity.MONTH);
            }
            default -> selectedRange;
        };
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String asText(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record DateRange(String period, LocalDate from, LocalDate to, BucketGranularity granularity) {}

    private enum BucketGranularity {
        DAY,
        MONTH
    }

    private static class ChartPointTotals {
        private BigDecimal value = BigDecimal.ZERO;
        private BigDecimal secondary = BigDecimal.ZERO;
    }

    private record PendingApprovalSummary(List<PendingApprovalDTO> rows, long totalCount) {}
}
