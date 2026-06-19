import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import {
  DashboardChartPoint,
  DashboardFilter,
  DashboardPeriod,
  DashboardSalesPurchasePoint,
  DashboardSummary,
  LowStockAlert,
  PendingApproval,
  RecentDocument,
  RecentTransaction,
  TopSellingProduct
} from '../dashboard.model';
import { DashboardService } from '../dashboard.service';

type KpiCard = {
  title: string;
  value: number;
  format: 'currency' | 'number' | 'percent';
  subtitle: string;
  tone: string;
  route?: string;
  visible: boolean;
};

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary = this.emptySummary();
  loading = false;
  errorMessage = '';
  filter: DashboardFilter = { period: 'month' };
  validationMessage = '';

  readonly periodOptions: { value: DashboardPeriod; label: string }[] = [
    { value: 'today', label: 'Today' },
    { value: 'month', label: 'This Month' },
    { value: 'year', label: 'This Year' },
    { value: 'custom', label: 'Custom' }
  ];

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.validationMessage = '';
    if (this.filter.period === 'custom' && (!this.filter.fromDate || !this.filter.toDate)) {
      this.validationMessage = 'Select both dates to load a custom dashboard range.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.getSummary(this.filter).subscribe({
      next: summary => {
        this.summary = summary;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Dashboard data could not be loaded.');
      }
    });
  }

  onPeriodChange(): void {
    if (this.filter.period !== 'custom') {
      this.filter.fromDate = undefined;
      this.filter.toDate = undefined;
      this.loadSummary();
    }
  }

  refresh(): void {
    this.loadSummary();
  }

  navigateTo(route?: string): void {
    if (route) {
      this.router.navigate([route]);
    }
  }

  get primaryCards(): KpiCard[] {
    return this.allCards.filter(card => card.visible).slice(0, 8);
  }

  get secondaryCards(): KpiCard[] {
    return this.allCards.filter(card => card.visible).slice(8, 12);
  }

  get allCards(): KpiCard[] {
    const summary = this.summary;
    return [
      {
        title: this.periodKpiTitle('Sales'),
        value: summary.periodSales,
        format: 'currency',
        subtitle: 'Posted and confirmed sales invoices',
        tone: 'sales',
        route: this.hasAnyPermission(['SALES_INVOICE_VIEW', 'SALES_VIEW']) ? '/sales/invoices' : undefined,
        visible: this.hasAnyPermission(['SALES_INVOICE_VIEW', 'SALES_VIEW'])
      },
      {
        title: this.periodKpiTitle('Purchase'),
        value: summary.periodPurchase,
        format: 'currency',
        subtitle: 'Received and posted purchases',
        tone: 'purchase',
        route: this.purchaseRoute(),
        visible: this.hasAnyPermission(['PURCHASE_INVOICE_VIEW', 'PURCHASE_ORDER_VIEW', 'PURCHASE_VIEW'])
      },
      {
        title: this.periodKpiTitle('Expense'),
        value: summary.periodExpense,
        format: 'currency',
        subtitle: 'Posted expenses only',
        tone: 'expense',
        route: this.hasPermission('EXPENSE_VIEW') ? '/expenses' : undefined,
        visible: this.hasAnyPermission(['EXPENSE_VIEW', 'EXPENSE_APPROVE'])
      },
      {
        title: 'Net Profit / Net Income',
        value: summary.netProfit,
        format: 'currency',
        subtitle: 'Profit and loss driven',
        tone: 'profit',
        route: this.hasPermission('ACCOUNTING_VIEW') ? '/accounting/profit-loss' : undefined,
        visible: this.hasPermission('ACCOUNTING_VIEW')
      },
      {
        title: 'Total Stock Value',
        value: summary.totalStockValue,
        format: 'currency',
        subtitle: 'Current warehouse valuation',
        tone: 'stock',
        route: this.stockValueRoute(),
        visible: this.hasAnyPermission(['REPORT_VIEW', 'INVENTORY_VIEW'])
      },
      {
        title: 'Customer Receivable',
        value: summary.customerReceivable,
        format: 'currency',
        subtitle: 'Customer due and aging logic',
        tone: 'customer',
        route: this.hasPermission('REPORT_VIEW') ? '/reports/view/customer-dues' : undefined,
        visible: this.hasPermission('REPORT_VIEW')
      },
      {
        title: 'Supplier Payable',
        value: summary.supplierPayable,
        format: 'currency',
        subtitle: 'AP reconciliation balance',
        tone: 'supplier',
        route: this.hasPermission('SUPPLIER_LEDGER_VIEW') ? '/suppliers/ap-reconciliation' : undefined,
        visible: this.hasPermission('SUPPLIER_LEDGER_VIEW')
      },
      {
        title: 'Cash + Bank Balance',
        value: summary.cashBankBalance,
        format: 'currency',
        subtitle: 'As of selected end date',
        tone: 'cash',
        route: this.hasPermission('FINANCIAL_DASHBOARD_VIEW') ? '/accounting/financial-dashboard' : undefined,
        visible: this.hasPermission('ACCOUNTING_VIEW') || this.hasPermission('FINANCIAL_DASHBOARD_VIEW')
      },
      {
        title: 'Low Stock Items',
        value: summary.lowStockItemsCount,
        format: 'number',
        subtitle: 'Based on reorder logic',
        tone: 'risk',
        route: this.lowStockRoute(),
        visible: this.hasAnyPermission(['REPORT_VIEW', 'INVENTORY_VIEW'])
      },
      {
        title: 'Pending Approvals',
        value: summary.pendingApprovalsCount,
        format: 'number',
        subtitle: 'Expenses, sales, purchases, transfers',
        tone: 'warning',
        route: this.pendingApprovalsRoute(),
        visible: this.hasAnyPermission(['EXPENSE_APPROVE', 'EXPENSE_VIEW'])
      },
      {
        title: 'Trial Balance Difference',
        value: summary.trialBalanceDifference,
        format: 'currency',
        subtitle: 'Accounting validation gap',
        tone: 'neutral',
        route: this.hasPermission('ACCOUNTING_VIEW') ? '/accounting/trial-balance' : undefined,
        visible: this.hasPermission('ACCOUNTING_VIEW')
      },
      {
        title: 'Budget Utilization',
        value: summary.budgetUtilization,
        format: 'percent',
        subtitle: 'Budget vs actual utilization',
        tone: 'budget',
        route: this.hasPermission('BUDGET_VIEW') ? '/accounting/budget-vs-actual' : undefined,
        visible: this.hasPermission('ACCOUNTING_VIEW') || this.hasPermission('BUDGET_VIEW')
      }
    ];
  }

  get visibleTopSellingProducts(): TopSellingProduct[] {
    return this.summary.topSellingProducts.slice(0, 6);
  }

  get visiblePendingApprovals(): PendingApproval[] {
    return this.summary.pendingApprovals.slice(0, 6);
  }

  get visibleLowStockAlerts(): LowStockAlert[] {
    return this.summary.lowStockAlerts.slice(0, 6);
  }

  get visibleRecentTransactions(): RecentTransaction[] {
    return this.summary.recentTransactions.slice(0, 6);
  }

  get visibleRecentSales(): RecentDocument[] {
    return this.summary.recentSales.slice(0, 6);
  }

  get visibleRecentPurchases(): RecentDocument[] {
    return this.summary.recentPurchases.slice(0, 6);
  }

  get incomeExpenseMax(): number {
    return this.maxFromChart(this.summary.monthlyIncomeExpense.flatMap(point => [point.value, point.secondaryValue]));
  }

  get salesPurchaseMax(): number {
    return this.maxFromChart(this.summary.salesPurchaseTrend.flatMap(point => [point.sales, point.purchase]));
  }

  get cashBankMax(): number {
    return this.maxFromChart(this.summary.cashBankTrend.flatMap(point => [point.value, point.secondaryValue]));
  }

  get topSellingMax(): number {
    return this.maxFromChart(this.visibleTopSellingProducts.map(item => item.amount));
  }

  get expenseCategoryMax(): number {
    return this.maxFromChart(this.summary.expenseByCategory.map(item => item.value));
  }

  get warehouseValueMax(): number {
    return this.maxFromChart(this.summary.warehouseStockValue.map(item => item.value));
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return this.authService.hasAnyPermission(permissions);
  }

  formatCardValue(card: KpiCard): string {
    if (card.format === 'percent') {
      return `${card.value.toFixed(2)}%`;
    }
    if (card.format === 'number') {
      return Intl.NumberFormat('en-US', { maximumFractionDigits: 0 }).format(card.value);
    }
    return `BDT ${Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(card.value)}`;
  }

  formatMoney(value: number): string {
    return `BDT ${Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value || 0)}`;
  }

  percentOf(value: number, max: number): number {
    if (!max || max <= 0) {
      return 0;
    }
    return Math.max(6, Math.round((value / max) * 100));
  }

  statusClass(status: string): string {
    const normalized = (status || '').toUpperCase();
    if (['PAID', 'POSTED', 'COMPLETED', 'CONFIRMED', 'RECEIVED', 'APPROVED'].includes(normalized)) {
      return 'status-good';
    }
    if (['PARTIAL', 'PARTIAL_PAID'].includes(normalized)) {
      return 'status-info';
    }
    if (['SUBMITTED', 'PENDING'].includes(normalized)) {
      return 'status-warn';
    }
    if (['REJECTED', 'REVERSED', 'CANCELLED'].includes(normalized)) {
      return 'status-bad';
    }
    return 'status-neutral';
  }

  approvalRoute(item: PendingApproval): string {
    const type = (item.type || '').toLowerCase();
    if (type.includes('expense')) {
      return this.pendingApprovalsRoute() || '/expenses';
    }
    if (type.includes('sales')) {
      return '/sales/invoices';
    }
    if (type.includes('purchase')) {
      return this.purchaseRoute() || '/purchases/orders';
    }
    if (type.includes('transfer')) {
      return '/inventory/transfers';
    }
    return '/dashboard';
  }

  recentTransactionRoute(item: RecentTransaction): string {
    const type = (item.type || '').toLowerCase();
    if (type.includes('sales')) {
      return '/sales/invoices';
    }
    if (type.includes('purchase')) {
      return this.purchaseRoute() || '/purchases/orders';
    }
    if (type.includes('expense')) {
      return '/expenses';
    }
    return '/dashboard';
  }

  asChartLabel(point: DashboardChartPoint | DashboardSalesPurchasePoint): string {
    return point.label || 'N/A';
  }

  get rangeLabel(): string {
    return this.periodOptions.find(option => option.value === this.summary.period)?.label || 'This Month';
  }

  get lastUpdatedLabel(): string {
    return this.summary.generatedAt ? new Date(this.summary.generatedAt).toLocaleString() : 'Not available';
  }

  private periodKpiTitle(base: string): string {
    if (this.summary.period === 'today') {
      return `Today's ${base}`;
    }
    return `${this.rangeLabel} ${base}`;
  }

  purchaseRoute(): string | undefined {
    if (this.hasPermission('PURCHASE_INVOICE_VIEW')) {
      return '/purchases/invoices';
    }
    if (this.hasPermission('PURCHASE_ORDER_VIEW') || this.hasPermission('PURCHASE_VIEW')) {
      return '/purchases/orders';
    }
    return undefined;
  }

  private stockValueRoute(): string | undefined {
    if (this.hasPermission('REPORT_VIEW')) {
      return '/reports/view/warehouse-stock-valuation';
    }
    if (this.hasPermission('INVENTORY_VIEW')) {
      return '/inventory/stocks';
    }
    return undefined;
  }

  lowStockRoute(): string | undefined {
    if (this.hasPermission('REPORT_VIEW')) {
      return '/reports/view/low-stock';
    }
    if (this.hasPermission('INVENTORY_VIEW')) {
      return '/inventory/stocks';
    }
    return undefined;
  }

  pendingApprovalsRoute(): string | undefined {
    if (this.hasPermission('EXPENSE_APPROVE')) {
      return '/expenses/approval-queue';
    }
    if (this.hasPermission('EXPENSE_VIEW')) {
      return '/expenses';
    }
    return undefined;
  }

  private maxFromChart(values: number[]): number {
    const max = values.reduce((current, value) => Math.max(current, value || 0), 0);
    return max > 0 ? max : 1;
  }

  private emptySummary(): DashboardSummary {
    return {
      period: 'month',
      fromDate: '',
      toDate: '',
      generatedAt: '',
      periodSales: 0,
      periodPurchase: 0,
      periodExpense: 0,
      netProfit: 0,
      totalStockValue: 0,
      customerReceivable: 0,
      supplierPayable: 0,
      cashBankBalance: 0,
      lowStockItemsCount: 0,
      pendingApprovalsCount: 0,
      trialBalanceDifference: 0,
      budgetUtilization: 0,
      todaySales: 0,
      todayPurchase: 0,
      todayExpense: 0,
      todayProfit: 0,
      customerDue: 0,
      supplierDue: 0,
      pendingApprovalExpenses: 0,
      monthlyIncomeExpense: [],
      expenseByCategory: [],
      warehouseStockValue: [],
      cashBankTrend: [],
      salesPurchaseTrend: [],
      monthlySalesPurchase: [],
      topSellingProducts: [],
      lowStockAlerts: [],
      dueAlerts: [],
      recentTransactions: [],
      pendingApprovals: [],
      recentSales: [],
      recentPurchases: []
    };
  }
}
