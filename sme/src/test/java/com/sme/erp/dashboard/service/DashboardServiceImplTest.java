package com.sme.erp.dashboard.service;

import com.sme.erp.accounting.dto.BudgetActualDTO;
import com.sme.erp.accounting.dto.ProfitLossDTO;
import com.sme.erp.accounting.dto.TrialBalanceDTO;
import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.entity.Expense;
import com.sme.erp.accounting.entity.ExpenseCategory;
import com.sme.erp.accounting.entity.JournalEntry;
import com.sme.erp.accounting.entity.JournalEntryLine;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.enums.ExpenseStatus;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.accounting.repository.ExpenseRepository;
import com.sme.erp.accounting.repository.JournalEntryLineRepository;
import com.sme.erp.accounting.service.AccountingReportService;
import com.sme.erp.accounting.service.BudgetService;
import com.sme.erp.dashboard.dto.DashboardSummaryDTO;
import com.sme.erp.dashboard.service.impl.DashboardServiceImpl;
import com.sme.erp.inventory.entity.StockTransfer;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.inventory.repository.StockRepository;
import com.sme.erp.inventory.repository.StockTransferRepository;
import com.sme.erp.purchase.entity.PurchaseOrder;
import com.sme.erp.purchase.enums.PurchaseStatus;
import com.sme.erp.purchase.repository.PurchaseOrderRepository;
import com.sme.erp.reports.dto.CustomerDueReportDTO;
import com.sme.erp.reports.dto.CustomerDueReportRowDTO;
import com.sme.erp.reports.dto.LowStockReportDTO;
import com.sme.erp.reports.dto.LowStockRowDTO;
import com.sme.erp.reports.dto.PurchaseReportDTO;
import com.sme.erp.reports.dto.PurchaseReportRowDTO;
import com.sme.erp.reports.dto.SalesReportDTO;
import com.sme.erp.reports.dto.SalesReportRowDTO;
import com.sme.erp.reports.dto.SupplierDueReportDTO;
import com.sme.erp.reports.dto.SupplierDueReportRowDTO;
import com.sme.erp.reports.dto.TopSellingProductReportDTO;
import com.sme.erp.reports.dto.TopSellingProductRowDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationReportDTO;
import com.sme.erp.reports.dto.WarehouseStockValuationRowDTO;
import com.sme.erp.reports.service.ReportService;
import com.sme.erp.sales.entity.SalesInvoice;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.repository.SalesInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private ReportService reportService;
    @Mock private AccountingReportService accountingReportService;
    @Mock private BudgetService budgetService;
    @Mock private AccountRepository accountRepository;
    @Mock private JournalEntryLineRepository journalEntryLineRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private StockRepository stockRepository;
    @Mock private SalesInvoiceRepository salesInvoiceRepository;
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private StockTransferRepository stockTransferRepository;

    private DashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DashboardServiceImpl(
                reportService,
                accountingReportService,
                budgetService,
                accountRepository,
                journalEntryLineRepository,
                expenseRepository,
                stockRepository,
                salesInvoiceRepository,
                purchaseOrderRepository,
                stockTransferRepository);
    }

    @Test
    void getSummary_returnsSafeEmptyDashboard() {
        stubAccounts();
        stubEmptySummaryData();

        DashboardSummaryDTO summary = service.getSummary("month", null, null);

        assertThat(summary.getPeriod()).isEqualTo("month");
        assertThat(summary.getPeriodSales()).isEqualByComparingTo("0.00");
        assertThat(summary.getPeriodPurchase()).isEqualByComparingTo("0.00");
        assertThat(summary.getPeriodExpense()).isEqualByComparingTo("0.00");
        assertThat(summary.getNetProfit()).isEqualByComparingTo("0.00");
        assertThat(summary.getCustomerReceivable()).isEqualByComparingTo("0.00");
        assertThat(summary.getSupplierPayable()).isEqualByComparingTo("0.00");
        assertThat(summary.getLowStockItemsCount()).isZero();
        assertThat(summary.getPendingApprovalsCount()).isZero();
        assertThat(summary.getTopSellingProducts()).isEmpty();
        assertThat(summary.getRecentTransactions()).isEmpty();
        assertThat(summary.getPendingApprovals()).isEmpty();
    }

    @Test
    void getSummary_aggregatesStableKpisChartsAndLists() {
        stubAccounts();
        stubEmptySummaryData();

        SalesReportDTO periodSales = new SalesReportDTO(
                new BigDecimal("1500.00"), new BigDecimal("900.00"), new BigDecimal("600.00"),
                2L, BigDecimal.ZERO, new BigDecimal("1500.00"),
                List.of(new SalesReportRowDTO("SI-1001", "Acme", "Main", "POSTED", LocalDateTime.now().minusDays(1),
                        new BigDecimal("5"), new BigDecimal("900.00"), new BigDecimal("500.00"), new BigDecimal("400.00"))));
        PurchaseReportDTO periodPurchase = new PurchaseReportDTO(
                new BigDecimal("800.00"), new BigDecimal("500.00"), new BigDecimal("300.00"),
                1L, BigDecimal.ZERO, new BigDecimal("800.00"),
                List.of(new PurchaseReportRowDTO("PO-1001", "Global Supplier", "Main", LocalDateTime.now().minusDays(1),
                        new BigDecimal("800.00"), new BigDecimal("500.00"), new BigDecimal("300.00"), PurchaseStatus.RECEIVED)));

        when(reportService.getSalesSummary(any(), any(), eq(null))).thenReturn(periodSales);
        when(reportService.getPurchaseSummary(any(), any(), eq(null))).thenReturn(periodPurchase);
        when(reportService.getSalesDetail(any(), any(), eq(null), eq(null), eq(null), eq(null))).thenReturn(periodSales);
        when(reportService.getPurchaseDetail(any(), any(), eq(null), eq(null), eq(null))).thenReturn(periodPurchase);
        when(reportService.getCustomerDueReport()).thenReturn(new CustomerDueReportDTO(
                new BigDecimal("600.00"), 1L, List.of(new CustomerDueReportRowDTO("Acme", new BigDecimal("1500.00"), new BigDecimal("900.00"), new BigDecimal("600.00")))));
        when(reportService.getSupplierDueReport()).thenReturn(new SupplierDueReportDTO(
                new BigDecimal("300.00"), 1L, List.of(new SupplierDueReportRowDTO("Global Supplier", new BigDecimal("800.00"), new BigDecimal("500.00"), new BigDecimal("300.00")))));
        when(reportService.getLowStockReport(null, null, null, null, null)).thenReturn(new LowStockReportDTO(
                1L, new BigDecimal("2.00"),
                List.of(new LowStockRowDTO("Cable", "CBL-1", "Main", new BigDecimal("3.00"), new BigDecimal("5.00"), new BigDecimal("2.00")))));
        when(reportService.getWarehouseStockValuation(null, null, null, null)).thenReturn(new WarehouseStockValuationReportDTO(
                1L, new BigDecimal("10.00"), new BigDecimal("1200.00"),
                List.of(new WarehouseStockValuationRowDTO("Main", 1L, new BigDecimal("10.00"), new BigDecimal("1200.00")))));
        when(reportService.getTopSellingProducts(any(), any(), eq(null), eq(null), eq(null), eq(null), eq(null))).thenReturn(
                new TopSellingProductReportDTO(
                        new BigDecimal("5.00"), new BigDecimal("900.00"), BigDecimal.ZERO, new BigDecimal("5.00"),
                        List.of(new TopSellingProductRowDTO(1L, "Cable", "CBL-1", new BigDecimal("5.00"), new BigDecimal("900.00"), BigDecimal.ZERO, new BigDecimal("5.00")))));
        when(accountingReportService.getProfitLoss(any(), any())).thenReturn(
                new ProfitLossDTO(List.of(), List.of(), new BigDecimal("1500.00"), new BigDecimal("700.00"), new BigDecimal("800.00"), false, BigDecimal.ZERO));
        when(accountingReportService.getTrialBalance(any(), any())).thenReturn(new TrialBalanceDTO(List.of(), new BigDecimal("1000.00"), new BigDecimal("1000.00")));
        when(budgetService.actual(eq(null), any(), any(), eq(null), eq(null))).thenReturn(
                new BudgetActualDTO(List.of(), new BigDecimal("1000.00"), new BigDecimal("450.00"), new BigDecimal("550.00"), new BigDecimal("45.00")));
        when(expenseRepository.sumActiveAmountBetween(any(), any())).thenReturn(new BigDecimal("250.00"));
        when(expenseRepository.search(any(), any(), eq(null), eq(null))).thenReturn(List.of(postedExpense("Utilities", "EXP-1001", new BigDecimal("250.00"))));
        when(expenseRepository.summarizePostedByCategory(any(), any())).thenReturn(List.<Object[]>of(new Object[]{"Utilities", new BigDecimal("250.00")}));
        when(stockRepository.sumInventoryValue()).thenReturn(new BigDecimal("1200.00"));
        when(expenseRepository.approvalQueue(null, null, null, null, null, null)).thenReturn(List.of(submittedExpense("Travel", "EXP-1002", new BigDecimal("80.00"))));
        when(salesInvoiceRepository.countSubmittedForApproval()).thenReturn(1L);
        when(salesInvoiceRepository.findSubmittedForApproval(any())).thenReturn(List.of(submittedInvoice()));
        when(purchaseOrderRepository.countSubmittedForApproval()).thenReturn(1L);
        when(purchaseOrderRepository.findSubmittedForApproval(any())).thenReturn(List.of(submittedOrder()));
        when(stockTransferRepository.findAll()).thenReturn(List.of(pendingTransfer()));
        when(journalEntryLineRepository.findPostedLedgerLines(eq(null), any(), any())).thenReturn(List.of(incomeLine(), expenseLine()));
        when(journalEntryLineRepository.sumDebit(eq(1L), eq(null), any())).thenReturn(new BigDecimal("300.00"));
        when(journalEntryLineRepository.sumCredit(eq(1L), eq(null), any())).thenReturn(new BigDecimal("50.00"));
        when(journalEntryLineRepository.sumDebit(eq(2L), eq(null), any())).thenReturn(new BigDecimal("400.00"));
        when(journalEntryLineRepository.sumCredit(eq(2L), eq(null), any())).thenReturn(new BigDecimal("100.00"));
        when(journalEntryLineRepository.openingBalance(eq(1L), any())).thenReturn(new BigDecimal("100.00"));
        when(journalEntryLineRepository.openingBalance(eq(2L), any())).thenReturn(new BigDecimal("200.00"));
        when(journalEntryLineRepository.findPostedLedgerLines(eq(1L), any(), any())).thenReturn(List.of(balanceLine(1L, new BigDecimal("50.00"), BigDecimal.ZERO)));
        when(journalEntryLineRepository.findPostedLedgerLines(eq(2L), any(), any())).thenReturn(List.of(balanceLine(2L, new BigDecimal("25.00"), BigDecimal.ZERO)));

        DashboardSummaryDTO summary = service.getSummary("custom", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertThat(summary.getPeriod()).isEqualTo("custom");
        assertThat(summary.getPeriodSales()).isEqualByComparingTo("1500.00");
        assertThat(summary.getPeriodPurchase()).isEqualByComparingTo("800.00");
        assertThat(summary.getPeriodExpense()).isEqualByComparingTo("250.00");
        assertThat(summary.getNetProfit()).isEqualByComparingTo("800.00");
        assertThat(summary.getTotalStockValue()).isEqualByComparingTo("1200.00");
        assertThat(summary.getCustomerReceivable()).isEqualByComparingTo("600.00");
        assertThat(summary.getSupplierPayable()).isEqualByComparingTo("300.00");
        assertThat(summary.getCashBankBalance()).isEqualByComparingTo("550.00");
        assertThat(summary.getLowStockItemsCount()).isEqualTo(1L);
        assertThat(summary.getPendingApprovalsCount()).isEqualTo(4L);
        assertThat(summary.getTrialBalanceDifference()).isEqualByComparingTo("0.00");
        assertThat(summary.getBudgetUtilization()).isEqualByComparingTo("45.00");
        assertThat(summary.getExpenseByCategory()).hasSize(1);
        assertThat(summary.getTopSellingProducts()).hasSize(1);
        assertThat(summary.getRecentTransactions()).hasSize(3);
        assertThat(summary.getPendingApprovals()).hasSize(4);
        assertThat(summary.getRecentSales()).hasSize(1);
        assertThat(summary.getRecentPurchases()).hasSize(1);
    }

    @Test
    void getSummary_forCustomRange_passesDatesToStableServices() {
        stubAccounts();
        stubEmptySummaryData();

        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 1, 31);

        service.getSummary("custom", fromDate, toDate);

        verify(reportService).getSalesSummary(fromDate, toDate, null);
        verify(reportService).getPurchaseSummary(fromDate, toDate, null);
        verify(accountingReportService).getProfitLoss(fromDate, toDate);
        verify(accountingReportService).getTrialBalance(fromDate, toDate);
        verify(budgetService).actual(null, fromDate, toDate, null, null);
    }

    private void stubAccounts() {
        Account incomeAccount = new Account();
        incomeAccount.setId(10L);
        incomeAccount.setAccountType(AccountType.INCOME);
        Account expenseAccount = new Account();
        expenseAccount.setId(11L);
        expenseAccount.setAccountType(AccountType.EXPENSE);
        Account cash = new Account();
        cash.setId(1L);
        cash.setAccountCode("1000");
        cash.setAccountName("Cash");
        cash.setAccountType(AccountType.ASSET);
        Account bank = new Account();
        bank.setId(2L);
        bank.setAccountCode("1010");
        bank.setAccountName("Bank");
        bank.setAccountType(AccountType.ASSET);

        when(accountRepository.findAll()).thenReturn(List.of(incomeAccount, expenseAccount, cash, bank));
        when(accountRepository.findByAccountCodeIgnoreCase("1000")).thenReturn(Optional.of(cash));
        when(accountRepository.findByAccountCodeIgnoreCase("1010")).thenReturn(Optional.of(bank));
    }

    private void stubEmptySummaryData() {
        SalesReportDTO emptySales = new SalesReportDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        PurchaseReportDTO emptyPurchase = new PurchaseReportDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO, BigDecimal.ZERO, List.of());

        when(reportService.getSalesSummary(any(), any(), eq(null))).thenReturn(emptySales);
        when(reportService.getPurchaseSummary(any(), any(), eq(null))).thenReturn(emptyPurchase);
        when(reportService.getSalesDetail(any(), any(), eq(null), eq(null), eq(null), eq(null))).thenReturn(emptySales);
        when(reportService.getPurchaseDetail(any(), any(), eq(null), eq(null), eq(null))).thenReturn(emptyPurchase);
        when(reportService.getCustomerDueReport()).thenReturn(new CustomerDueReportDTO(BigDecimal.ZERO, 0L, List.of()));
        when(reportService.getSupplierDueReport()).thenReturn(new SupplierDueReportDTO(BigDecimal.ZERO, 0L, List.of()));
        when(reportService.getLowStockReport(null, null, null, null, null)).thenReturn(new LowStockReportDTO(0L, BigDecimal.ZERO, List.of()));
        when(reportService.getWarehouseStockValuation(null, null, null, null)).thenReturn(new WarehouseStockValuationReportDTO(0L, BigDecimal.ZERO, BigDecimal.ZERO, List.of()));
        when(reportService.getTopSellingProducts(any(), any(), eq(null), eq(null), eq(null), eq(null), eq(null))).thenReturn(
                new TopSellingProductReportDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of()));
        when(accountingReportService.getProfitLoss(any(), any())).thenReturn(new ProfitLossDTO(List.of(), List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO));
        when(accountingReportService.getTrialBalance(any(), any())).thenReturn(new TrialBalanceDTO(List.of(), BigDecimal.ZERO, BigDecimal.ZERO));
        when(budgetService.actual(eq(null), any(), any(), eq(null), eq(null))).thenReturn(new BudgetActualDTO(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        when(expenseRepository.sumActiveAmountBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.search(any(), any(), eq(null), eq(null))).thenReturn(List.of());
        when(expenseRepository.summarizePostedByCategory(any(), any())).thenReturn(List.of());
        when(stockRepository.sumInventoryValue()).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.approvalQueue(null, null, null, null, null, null)).thenReturn(List.of());
        when(salesInvoiceRepository.countSubmittedForApproval()).thenReturn(0L);
        when(salesInvoiceRepository.findSubmittedForApproval(any())).thenReturn(List.of());
        when(purchaseOrderRepository.countSubmittedForApproval()).thenReturn(0L);
        when(purchaseOrderRepository.findSubmittedForApproval(any())).thenReturn(List.of());
        when(stockTransferRepository.findAll()).thenReturn(List.of());
        when(journalEntryLineRepository.findPostedLedgerLines(eq(null), any(), any())).thenReturn(List.of());
        when(journalEntryLineRepository.sumDebit(any(), eq(null), any())).thenReturn(BigDecimal.ZERO);
        when(journalEntryLineRepository.sumCredit(any(), eq(null), any())).thenReturn(BigDecimal.ZERO);
        when(journalEntryLineRepository.openingBalance(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    private Expense postedExpense(String categoryName, String expenseNo, BigDecimal amount) {
        Expense expense = new Expense();
        expense.setExpenseNo(expenseNo);
        expense.setExpenseDate(LocalDate.now());
        expense.setAmount(amount);
        expense.setStatus(ExpenseStatus.POSTED);
        ExpenseCategory category = new ExpenseCategory();
        category.setName(categoryName);
        expense.setCategory(category);
        return expense;
    }

    private Expense submittedExpense(String categoryName, String expenseNo, BigDecimal amount) {
        Expense expense = postedExpense(categoryName, expenseNo, amount);
        expense.setStatus(ExpenseStatus.SUBMITTED);
        return expense;
    }

    private SalesInvoice submittedInvoice() {
        SalesInvoice invoice = new SalesInvoice();
        invoice.setInvoiceNo("SI-PENDING");
        invoice.setSaleDate(LocalDate.now().atStartOfDay());
        invoice.setNetTotal(new BigDecimal("90.00"));
        invoice.setStatus(SalesInvoiceStatus.DRAFT);
        return invoice;
    }

    private PurchaseOrder submittedOrder() {
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseCode("PO-PENDING");
        order.setPurchaseDate(LocalDateTime.now());
        order.setNetTotal(new BigDecimal("70.00"));
        order.setStatus(PurchaseStatus.SUBMITTED);
        return order;
    }

    private StockTransfer pendingTransfer() {
        StockTransfer transfer = new StockTransfer();
        transfer.setId(99L);
        transfer.setTransferNo("TR-1001");
        transfer.setTransferDate(LocalDate.now());
        transfer.setStatus(StockTransferStatus.PENDING);
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Secondary");
        transfer.setToWarehouse(warehouse);
        return transfer;
    }

    private JournalEntryLine incomeLine() {
        return ledgerLine(10L, new BigDecimal("0.00"), new BigDecimal("1000.00"));
    }

    private JournalEntryLine expenseLine() {
        return ledgerLine(11L, new BigDecimal("250.00"), new BigDecimal("0.00"));
    }

    private JournalEntryLine balanceLine(Long accountId, BigDecimal debit, BigDecimal credit) {
        return ledgerLine(accountId, debit, credit);
    }

    private JournalEntryLine ledgerLine(Long accountId, BigDecimal debit, BigDecimal credit) {
        Account account = new Account();
        account.setId(accountId);
        JournalEntry entry = new JournalEntry();
        entry.setJournalDate(LocalDate.now());
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setJournalEntry(entry);
        line.setDebit(debit);
        line.setCredit(credit);
        return line;
    }
}
