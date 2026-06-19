import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Observable, of } from 'rxjs';
import { Brand } from '../../models/brand.model';
import { ProductCategory } from '../../models/category.model';
import { CustomerOption } from '../../models/customer.model';
import { InventoryWarehouse } from '../../models/inventory-warehouse.model';
import { Product } from '../../models/product.model';
import { SupplierOption } from '../../models/supplier.model';
import { ProductBrandService } from '../../services/product-brand.service';
import { ProductCategoryService } from '../../services/product-category.service';
import { CustomerService } from '../../services/customer.service';
import { InventoryWarehouseService } from '../../services/inventory-warehouse.service';
import { ProductService } from '../../services/product.service';
import { SupplierService } from '../../services/supplier.service';
import { AuthService } from '../auth/auth.service';
import {
  CustomerDueReport,
  CustomerSalesReport,
  LowStockReport,
  ProfitLossSummary,
  PurchaseReport,
  PurchaseReturnReport,
  ReportFilters,
  ReportsService,
  SalesReport,
  StockReport,
  StockTransferReport,
  SupplierDueReport,
  SupplierPurchaseReport,
  TopSellingProductReport,
  WarehouseStockValuationReport
} from './reports.service';

type ReportMode = 'center' | 'detail';
type DetailReportType =
  | 'sales-summary'
  | 'sales-detail'
  | 'top-selling-products'
  | 'customer-sales'
  | 'purchase-summary'
  | 'purchase-detail'
  | 'supplier-purchases'
  | 'purchase-returns'
  | 'stock'
  | 'stock-movements'
  | 'low-stock'
  | 'warehouse-stock-valuation'
  | 'stock-transfers'
  | 'customer-dues'
  | 'supplier-dues'
  | 'profit-loss';

interface ReportCard {
  title: string;
  description: string;
  category: string;
  route: string;
  internal: boolean;
  permissions?: string[];
  anyPermissions?: string[];
}

interface SummaryCard {
  label: string;
  value: number | string;
  type?: 'currency' | 'number' | 'text';
  accent?: string;
}

interface TableColumn {
  key: string;
  label: string;
  type?: 'currency' | 'number' | 'date' | 'datetime' | 'text';
}

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  mode: ReportMode = 'center';
  reportType: DetailReportType = 'sales-summary';
  loading = false;
  error = '';

  centerSearch = '';
  categoryFilter = 'All';
  readonly categories = ['All', 'Sales', 'Purchase', 'Inventory', 'Customer', 'Supplier', 'Expense', 'Accounting'];

  filters: ReportFilters = {};
  customerSearch = '';
  supplierSearch = '';
  customers: CustomerOption[] = [];
  suppliers: SupplierOption[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  categoriesList: ProductCategory[] = [];
  brands: Brand[] = [];

  summaryCards: SummaryCard[] = [];
  tableColumns: TableColumn[] = [];
  tableRows: Array<Record<string, unknown>> = [];
  emptyMessage = 'No data found for the selected filters.';

  readonly cards: ReportCard[] = [
    { title: 'Sales Summary', description: 'Revenue, collections, due, and return totals for posted invoices.', category: 'Sales', route: '/reports/view/sales-summary', internal: true },
    { title: 'Sales Detail', description: 'Invoice-level sales lines by date, customer, warehouse, and status.', category: 'Sales', route: '/reports/view/sales-detail', internal: true },
    { title: 'Top Selling Products', description: 'Best-selling products ranked by sold quantity and gross sales.', category: 'Sales', route: '/reports/view/top-selling-products', internal: true },
    { title: 'Customer Sales', description: 'Customer-wise sales, collections, due, and latest sale activity.', category: 'Sales', route: '/reports/view/customer-sales', internal: true },
    { title: 'Purchase Summary', description: 'Purchase value, payments, due, and return impact for posted orders.', category: 'Purchase', route: '/reports/view/purchase-summary', internal: true },
    { title: 'Purchase Detail', description: 'PO-level purchase details with supplier, warehouse, and status.', category: 'Purchase', route: '/reports/view/purchase-detail', internal: true },
    { title: 'Supplier Purchase', description: 'Supplier-wise purchase volume, payments, due, and recent activity.', category: 'Purchase', route: '/reports/view/supplier-purchases', internal: true },
    { title: 'Purchase Return Report', description: 'Supplier purchase returns with reference numbers and returned value.', category: 'Purchase', route: '/reports/view/purchase-returns', internal: true },
    { title: 'Stock Report', description: 'Current stock by product, SKU, category, brand, and warehouse.', category: 'Inventory', route: '/reports/view/stock', internal: true },
    { title: 'Stock Movement Report', description: 'Inbound and outbound stock movement trail with before/after quantities.', category: 'Inventory', route: '/reports/view/stock-movements', internal: true },
    { title: 'Low Stock Report', description: 'Products at or below reorder level with shortage quantity.', category: 'Inventory', route: '/reports/view/low-stock', internal: true },
    { title: 'Warehouse Stock Valuation', description: 'Warehouse-wise quantity and inventory valuation snapshot.', category: 'Inventory', route: '/reports/view/warehouse-stock-valuation', internal: true },
    { title: 'Stock Transfer Report', description: 'Transfer activity by source, destination, status, and quantity.', category: 'Inventory', route: '/reports/view/stock-transfers', internal: true },
    { title: 'Customer Due Report', description: 'Outstanding customer balances from confirmed and completed invoices.', category: 'Customer', route: '/reports/view/customer-dues', internal: true },
    { title: 'Customer Ledger Report', description: 'Opens the accounting customer ledger report.', category: 'Customer', route: '/accounting/customer-ledger', internal: false, permissions: ['ACCOUNTING_VIEW'] },
    { title: 'Customer Aging Report', description: 'Opens the customer aging report with customer search filters.', category: 'Customer', route: '/customers/aging', internal: false, permissions: ['CUSTOMER_AGING_VIEW'] },
    { title: 'Customer Statement', description: 'Open a customer detail record to view and print customer statement.', category: 'Customer', route: '/customers/list', internal: false, permissions: ['CUSTOMER_VIEW'] },
    { title: 'Supplier Due Report', description: 'Outstanding supplier balances from posted purchase orders.', category: 'Supplier', route: '/reports/view/supplier-dues', internal: true },
    { title: 'Supplier Ledger Report', description: 'Opens the accounting supplier ledger report.', category: 'Supplier', route: '/accounting/supplier-ledger', internal: false, permissions: ['ACCOUNTING_VIEW'] },
    { title: 'Supplier Aging Report', description: 'Opens the supplier aging report with supplier search filters.', category: 'Supplier', route: '/suppliers/aging', internal: false, anyPermissions: ['SUPPLIER_LEDGER_VIEW', 'SUPPLIER_VIEW'] },
    { title: 'Supplier Statement', description: 'Open a supplier detail record to view and print supplier statement.', category: 'Supplier', route: '/suppliers/list', internal: false, permissions: ['SUPPLIER_VIEW'] },
    { title: 'AP Reconciliation Report', description: 'Supplier due, advance, GL payable, and variance reconciliation.', category: 'Supplier', route: '/suppliers/ap-reconciliation', internal: false, permissions: ['SUPPLIER_LEDGER_VIEW'] },
    { title: 'Expense Summary', description: 'Opens the existing expense report center.', category: 'Expense', route: '/expenses/reports', internal: false, permissions: ['EXPENSE_REPORT_VIEW'] },
    { title: 'Expense Category Report', description: 'Opens the existing expense report center category view.', category: 'Expense', route: '/expenses/reports', internal: false, permissions: ['EXPENSE_REPORT_VIEW'] },
    { title: 'Expense Payment Method Report', description: 'Opens the existing expense report center payment method view.', category: 'Expense', route: '/expenses/reports', internal: false, permissions: ['EXPENSE_REPORT_VIEW'] },
    { title: 'Expense Tax Report', description: 'Opens the existing expense report center tax view.', category: 'Expense', route: '/expenses/reports', internal: false, permissions: ['EXPENSE_REPORT_VIEW'] },
    { title: 'Monthly Expense Trend', description: 'Opens the existing expense trend reporting page.', category: 'Expense', route: '/expenses/reports', internal: false, permissions: ['EXPENSE_REPORT_VIEW'] },
    { title: 'Trial Balance', description: 'Opens the accounting trial balance report.', category: 'Accounting', route: '/accounting/trial-balance', internal: false, anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { title: 'General Ledger', description: 'Opens the accounting general ledger report.', category: 'Accounting', route: '/accounting/general-ledger', internal: false, anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { title: 'Account Ledger', description: 'Open the chart of accounts first to drill into an account ledger.', category: 'Accounting', route: '/accounting/accounts', internal: false, anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { title: 'Profit & Loss', description: 'Opens the accounting profit and loss statement.', category: 'Accounting', route: '/accounting/profit-loss', internal: false, anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { title: 'Balance Sheet', description: 'Opens the accounting balance sheet report.', category: 'Accounting', route: '/accounting/balance-sheet', internal: false, anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { title: 'Cash Book', description: 'Opens the accounting cash book.', category: 'Accounting', route: '/accounting/cash-book', internal: false, permissions: ['ACCOUNTING_VIEW'] },
    { title: 'Bank Book', description: 'Opens the accounting bank book.', category: 'Accounting', route: '/accounting/bank-book', internal: false, permissions: ['ACCOUNTING_VIEW'] },
    { title: 'Budget vs Actual', description: 'Opens the accounting budget versus actual report.', category: 'Accounting', route: '/accounting/budget-vs-actual', internal: false, permissions: ['ACCOUNTING_VIEW'] }
  ];

  readonly detailTitles: Record<DetailReportType, string> = {
    'sales-summary': 'Sales Summary',
    'sales-detail': 'Sales Detail',
    'top-selling-products': 'Top Selling Products',
    'customer-sales': 'Customer Sales',
    'purchase-summary': 'Purchase Summary',
    'purchase-detail': 'Purchase Detail',
    'supplier-purchases': 'Supplier Purchase',
    'purchase-returns': 'Purchase Return Report',
    stock: 'Stock Report',
    'stock-movements': 'Stock Movement Report',
    'low-stock': 'Low Stock Report',
    'warehouse-stock-valuation': 'Warehouse Stock Valuation',
    'stock-transfers': 'Stock Transfer Report',
    'customer-dues': 'Customer Due Report',
    'supplier-dues': 'Supplier Due Report',
    'profit-loss': 'Profit & Loss Summary'
  };

  constructor(
    private route: ActivatedRoute,
    private reportsService: ReportsService,
    private customerService: CustomerService,
    private supplierService: SupplierService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private categoryService: ProductCategoryService,
    private brandService: ProductBrandService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.route.data.subscribe(data => {
      this.mode = (data['mode'] as ReportMode) || 'detail';
      if (this.mode === 'center') {
        this.resetDetailState();
      }
    });

    this.route.paramMap.subscribe(params => {
      const type = params.get('type');
      if (!type) {
        return;
      }
      this.mode = 'detail';
      this.reportType = this.normalizeReportType(type);
      this.resetDetailState();
      this.loadReport();
    });
  }

  get pageTitle(): string {
    return this.mode === 'center' ? 'Report Center' : this.detailTitles[this.reportType];
  }

  get visibleCards(): ReportCard[] {
    return this.cards.filter(card => this.canSeeCard(card)).filter(card => {
      const matchesCategory = this.categoryFilter === 'All' || card.category === this.categoryFilter;
      const term = this.centerSearch.trim().toLowerCase();
      const matchesSearch = !term
        || card.title.toLowerCase().includes(term)
        || card.description.toLowerCase().includes(term)
        || card.category.toLowerCase().includes(term);
      return matchesCategory && matchesSearch;
    });
  }

  get hasRows(): boolean {
    return this.tableRows.length > 0;
  }

  get canExportCsv(): boolean {
    return this.hasPermission('REPORT_EXPORT_CSV') || this.hasPermission('REPORT_EXPORT');
  }

  get canExportExcel(): boolean {
    return this.hasPermission('REPORT_EXPORT_EXCEL') || this.hasPermission('REPORT_EXPORT');
  }

  get canPrintReport(): boolean {
    return this.hasPermission('REPORT_PRINT') || this.hasPermission('REPORT_VIEW');
  }

  get showDateFilters(): boolean {
    return !['customer-dues', 'supplier-dues', 'stock', 'low-stock', 'warehouse-stock-valuation'].includes(this.reportType);
  }

  get showStatusFilter(): boolean {
    return this.reportType === 'stock-transfers';
  }

  get showKeywordFilter(): boolean {
    return !['customer-dues', 'supplier-dues', 'profit-loss'].includes(this.reportType);
  }

  get showCustomerFilter(): boolean {
    return ['sales-summary', 'sales-detail', 'customer-sales'].includes(this.reportType);
  }

  get showSupplierFilter(): boolean {
    return ['purchase-summary', 'purchase-detail', 'supplier-purchases', 'purchase-returns'].includes(this.reportType);
  }

  get showProductFilter(): boolean {
    return ['sales-detail', 'top-selling-products', 'stock', 'stock-movements', 'low-stock'].includes(this.reportType);
  }

  get showWarehouseFilter(): boolean {
    return ['sales-detail', 'purchase-detail', 'top-selling-products', 'stock', 'stock-movements', 'low-stock', 'warehouse-stock-valuation', 'stock-transfers'].includes(this.reportType);
  }

  get showCategoryFilter(): boolean {
    return ['top-selling-products', 'stock', 'low-stock', 'warehouse-stock-valuation'].includes(this.reportType);
  }

  get showBrandFilter(): boolean {
    return ['top-selling-products', 'stock', 'low-stock', 'warehouse-stock-valuation'].includes(this.reportType);
  }

  loadReport(): void {
    if (this.mode !== 'detail') {
      return;
    }

    this.loading = true;
    this.error = '';
    this.summaryCards = [];
    this.tableColumns = [];
    this.tableRows = [];

    this.reportRequest().subscribe({
      next: report => {
        this.bindReport(report);
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load report data.';
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.filters = {};
    this.customerSearch = '';
    this.supplierSearch = '';
    this.loadReport();
  }

  searchCustomers(): void {
    this.customerService.searchCustomers(this.customerSearch).subscribe({
      next: customers => this.customers = customers,
      error: () => this.customers = []
    });
  }

  searchSuppliers(): void {
    this.supplierService.getSupplierOptions(this.supplierSearch).subscribe({
      next: suppliers => this.suppliers = suppliers,
      error: () => this.suppliers = []
    });
  }

  exportCsv(): void {
    this.download(this.csv(), `${this.reportType}.csv`, 'text/csv;charset=utf-8;');
  }

  exportExcel(): void {
    this.download(this.csv(), `${this.reportType}.xls`, 'application/vnd.ms-excel;charset=utf-8;');
  }

  print(): void {
    window.print();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  formatCell(row: Record<string, unknown>, column: TableColumn): string {
    const value = row[column.key];
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (column.type === 'currency') {
      return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    if (column.type === 'number') {
      return Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 });
    }
    if (column.type === 'date' || column.type === 'datetime') {
      const parsed = new Date(String(value));
      return Number.isNaN(parsed.getTime())
        ? String(value)
        : column.type === 'datetime'
          ? parsed.toLocaleString()
          : parsed.toLocaleDateString();
    }
    return String(value);
  }

  private loadOptions(): void {
    forkJoin({
      customers: this.customerService.searchCustomers(''),
      suppliers: this.supplierService.getSupplierOptions(''),
      products: this.productService.getAllProducts(),
      warehouses: this.warehouseService.getAllWarehouses(),
      categories: this.categoryService.getAllCategories(),
      brands: this.brandService.getAllBrands()
    }).subscribe({
      next: options => {
        this.customers = options.customers;
        this.suppliers = options.suppliers;
        this.products = options.products;
        this.warehouses = options.warehouses;
        this.categoriesList = options.categories;
        this.brands = options.brands;
      },
      error: () => {
        this.customers = [];
        this.suppliers = [];
        this.products = [];
        this.warehouses = [];
        this.categoriesList = [];
        this.brands = [];
      }
    });
  }

  private reportRequest(): Observable<unknown> {
    if (this.reportType === 'sales-summary') return this.reportsService.getSalesSummary(this.filters);
    if (this.reportType === 'sales-detail') return this.reportsService.getSalesDetail(this.filters);
    if (this.reportType === 'top-selling-products') return this.reportsService.getTopSellingProducts(this.filters);
    if (this.reportType === 'customer-sales') return this.reportsService.getCustomerSales(this.filters);
    if (this.reportType === 'purchase-summary') return this.reportsService.getPurchaseSummary(this.filters);
    if (this.reportType === 'purchase-detail') return this.reportsService.getPurchaseDetail(this.filters);
    if (this.reportType === 'supplier-purchases') return this.reportsService.getSupplierPurchases(this.filters);
    if (this.reportType === 'purchase-returns') return this.reportsService.getPurchaseReturns(this.filters);
    if (this.reportType === 'stock') return this.reportsService.getStockReport(this.filters);
    if (this.reportType === 'stock-movements') return this.reportsService.getStockMovements(this.filters);
    if (this.reportType === 'low-stock') return this.reportsService.getLowStock(this.filters);
    if (this.reportType === 'warehouse-stock-valuation') return this.reportsService.getWarehouseStockValuation(this.filters);
    if (this.reportType === 'stock-transfers') return this.reportsService.getStockTransfers(this.filters);
    if (this.reportType === 'customer-dues') return this.reportsService.getCustomerDueReport();
    if (this.reportType === 'supplier-dues') return this.reportsService.getSupplierDueReport();
    return this.reportsService.getProfitLossSummary(this.filters);
  }

  private bindReport(report: unknown): void {
    if (this.reportType === 'sales-summary' || this.reportType === 'sales-detail') {
      this.bindSales(report as SalesReport);
      return;
    }
    if (this.reportType === 'top-selling-products') {
      this.bindTopSelling(report as TopSellingProductReport);
      return;
    }
    if (this.reportType === 'customer-sales') {
      this.bindCustomerSales(report as CustomerSalesReport);
      return;
    }
    if (this.reportType === 'purchase-summary' || this.reportType === 'purchase-detail') {
      this.bindPurchase(report as PurchaseReport);
      return;
    }
    if (this.reportType === 'supplier-purchases') {
      this.bindSupplierPurchases(report as SupplierPurchaseReport);
      return;
    }
    if (this.reportType === 'purchase-returns') {
      this.bindPurchaseReturns(report as PurchaseReturnReport);
      return;
    }
    if (this.reportType === 'stock') {
      this.bindStock(report as StockReport);
      return;
    }
    if (this.reportType === 'stock-movements') {
      this.bindStockMovements(report as StockReport);
      return;
    }
    if (this.reportType === 'low-stock') {
      this.bindLowStock(report as LowStockReport);
      return;
    }
    if (this.reportType === 'warehouse-stock-valuation') {
      this.bindWarehouseValuation(report as WarehouseStockValuationReport);
      return;
    }
    if (this.reportType === 'stock-transfers') {
      this.bindStockTransfers(report as StockTransferReport);
      return;
    }
    if (this.reportType === 'customer-dues') {
      this.bindCustomerDue(report as CustomerDueReport);
      return;
    }
    if (this.reportType === 'supplier-dues') {
      this.bindSupplierDue(report as SupplierDueReport);
      return;
    }
    this.bindProfitLoss(report as ProfitLossSummary);
  }

  private bindSales(report: SalesReport): void {
    this.summaryCards = [
      { label: 'Total Sales', value: report.totalSales, type: 'currency' },
      { label: 'Total Paid', value: report.totalPaid, type: 'currency', accent: 'accent-blue' },
      { label: 'Total Due', value: report.totalDue, type: 'currency', accent: 'accent-amber' },
      { label: 'Return Amount', value: report.returnAmount, type: 'currency', accent: 'accent-neutral' },
      { label: 'Net Sales', value: report.netSales, type: 'currency', accent: 'accent-profit' },
      { label: 'Invoice Count', value: report.totalInvoices, type: 'number' }
    ];
    this.tableColumns = [
      { key: 'date', label: 'Date', type: 'datetime' },
      { key: 'invoiceNo', label: 'Invoice No' },
      { key: 'customer', label: 'Customer' },
      { key: 'warehouse', label: 'Warehouse' },
      { key: 'status', label: 'Status' },
      { key: 'quantity', label: 'Qty', type: 'number' },
      { key: 'amount', label: 'Net Total', type: 'currency' },
      { key: 'paid', label: 'Paid', type: 'currency' },
      { key: 'due', label: 'Due', type: 'currency' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindTopSelling(report: TopSellingProductReport): void {
    this.summaryCards = [
      { label: 'Quantity Sold', value: report.totalQuantitySold, type: 'number' },
      { label: 'Gross Sales', value: report.totalGrossSales, type: 'currency', accent: 'accent-blue' },
      { label: 'Return Qty', value: report.totalReturnQty, type: 'number', accent: 'accent-amber' },
      { label: 'Net Qty', value: report.totalNetQty, type: 'number', accent: 'accent-profit' }
    ];
    this.tableColumns = [
      { key: 'product', label: 'Product' },
      { key: 'sku', label: 'SKU' },
      { key: 'quantitySold', label: 'Quantity Sold', type: 'number' },
      { key: 'grossSales', label: 'Gross Sales', type: 'currency' },
      { key: 'returnQty', label: 'Return Qty', type: 'number' },
      { key: 'netQty', label: 'Net Qty', type: 'number' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindCustomerSales(report: CustomerSalesReport): void {
    this.summaryCards = [
      { label: 'Customers', value: report.totalCustomers, type: 'number' },
      { label: 'Total Sales', value: report.totalSales, type: 'currency', accent: 'accent-blue' },
      { label: 'Paid Amount', value: report.totalPaid, type: 'currency', accent: 'accent-profit' },
      { label: 'Due Amount', value: report.totalDue, type: 'currency', accent: 'accent-amber' }
    ];
    this.tableColumns = [
      { key: 'customer', label: 'Customer' },
      { key: 'invoiceCount', label: 'Invoice Count', type: 'number' },
      { key: 'totalSales', label: 'Total Sales', type: 'currency' },
      { key: 'paidAmount', label: 'Paid Amount', type: 'currency' },
      { key: 'dueAmount', label: 'Due Amount', type: 'currency' },
      { key: 'lastSaleDate', label: 'Last Sale Date', type: 'datetime' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindPurchase(report: PurchaseReport): void {
    this.summaryCards = [
      { label: 'Total Purchase', value: report.totalPurchase, type: 'currency' },
      { label: 'Total Paid', value: report.totalPaid, type: 'currency', accent: 'accent-blue' },
      { label: 'Total Due', value: report.totalDue, type: 'currency', accent: 'accent-amber' },
      { label: 'Return Amount', value: report.returnAmount, type: 'currency', accent: 'accent-neutral' },
      { label: 'Net Purchase', value: report.netPurchase, type: 'currency', accent: 'accent-profit' },
      { label: 'Order Count', value: report.totalOrders, type: 'number' }
    ];
    this.tableColumns = [
      { key: 'date', label: 'Date', type: 'datetime' },
      { key: 'poNo', label: 'Purchase No' },
      { key: 'supplier', label: 'Supplier' },
      { key: 'warehouse', label: 'Warehouse' },
      { key: 'status', label: 'Status' },
      { key: 'amount', label: 'Net Total', type: 'currency' },
      { key: 'paid', label: 'Paid', type: 'currency' },
      { key: 'due', label: 'Due', type: 'currency' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindSupplierPurchases(report: SupplierPurchaseReport): void {
    this.summaryCards = [
      { label: 'Suppliers', value: report.totalSuppliers, type: 'number' },
      { label: 'Total Purchase', value: report.totalPurchase, type: 'currency', accent: 'accent-blue' },
      { label: 'Paid Amount', value: report.totalPaid, type: 'currency', accent: 'accent-profit' },
      { label: 'Due Amount', value: report.totalDue, type: 'currency', accent: 'accent-amber' }
    ];
    this.tableColumns = [
      { key: 'supplier', label: 'Supplier' },
      { key: 'purchaseCount', label: 'Purchase Count', type: 'number' },
      { key: 'totalPurchase', label: 'Total Purchase', type: 'currency' },
      { key: 'paidAmount', label: 'Paid Amount', type: 'currency' },
      { key: 'dueAmount', label: 'Due Amount', type: 'currency' },
      { key: 'lastPurchaseDate', label: 'Last Purchase Date', type: 'datetime' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindPurchaseReturns(report: PurchaseReturnReport): void {
    this.summaryCards = [
      { label: 'Return Count', value: report.returnCount, type: 'number' },
      { label: 'Total Return Amount', value: report.totalReturnAmount, type: 'currency', accent: 'accent-amber' }
    ];
    this.tableColumns = [
      { key: 'date', label: 'Date', type: 'datetime' },
      { key: 'returnNo', label: 'Return No' },
      { key: 'supplier', label: 'Supplier' },
      { key: 'purchaseNo', label: 'Purchase No' },
      { key: 'amount', label: 'Amount', type: 'currency' },
      { key: 'status', label: 'Status' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindStock(report: StockReport): void {
    this.summaryCards = [
      { label: 'Total Quantity', value: report.totalStockQuantity, type: 'number' },
      { label: 'Stock Value', value: report.totalStockValue, type: 'currency', accent: 'accent-blue' },
      { label: 'Low Stock Count', value: report.lowStockCount, type: 'number', accent: 'accent-amber' }
    ];
    this.tableColumns = [
      { key: 'product', label: 'Product' },
      { key: 'sku', label: 'SKU' },
      { key: 'category', label: 'Category' },
      { key: 'brand', label: 'Brand' },
      { key: 'warehouse', label: 'Warehouse' },
      { key: 'quantity', label: 'Quantity', type: 'number' },
      { key: 'reorderLevel', label: 'Reorder Level', type: 'number' },
      { key: 'status', label: 'Status' },
      { key: 'stockValue', label: 'Stock Value', type: 'currency' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindStockMovements(report: StockReport): void {
    this.summaryCards = [
      { label: 'Movement Rows', value: report.movements.length, type: 'number' },
      { label: 'Quantity Moved', value: report.totalStockQuantity, type: 'number', accent: 'accent-blue' }
    ];
    this.tableColumns = [
      { key: 'date', label: 'Date', type: 'datetime' },
      { key: 'product', label: 'Product' },
      { key: 'warehouse', label: 'Warehouse' },
      { key: 'movementType', label: 'Movement Type' },
      { key: 'quantity', label: 'Quantity', type: 'number' },
      { key: 'quantityBefore', label: 'Before', type: 'number' },
      { key: 'quantityChange', label: 'Change', type: 'number' },
      { key: 'quantityAfter', label: 'After', type: 'number' },
      { key: 'referenceNo', label: 'Reference No' }
    ];
    this.tableRows = this.asTableRows(report.movements);
  }

  private bindLowStock(report: LowStockReport): void {
    this.summaryCards = [
      { label: 'Low Stock Items', value: report.totalLowStockItems, type: 'number', accent: 'accent-amber' },
      { label: 'Shortage Quantity', value: report.totalShortageQty, type: 'number' }
    ];
    this.tableColumns = [
      { key: 'product', label: 'Product' },
      { key: 'sku', label: 'SKU' },
      { key: 'warehouse', label: 'Warehouse' },
      { key: 'currentQty', label: 'Current Qty', type: 'number' },
      { key: 'reorderLevel', label: 'Reorder Level', type: 'number' },
      { key: 'shortageQty', label: 'Shortage Qty', type: 'number' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindWarehouseValuation(report: WarehouseStockValuationReport): void {
    this.summaryCards = [
      { label: 'Warehouses', value: report.totalWarehouses, type: 'number' },
      { label: 'Total Quantity', value: report.totalQuantity, type: 'number', accent: 'accent-blue' },
      { label: 'Stock Value', value: report.totalStockValue, type: 'currency', accent: 'accent-profit' }
    ];
    this.tableColumns = [
      { key: 'warehouse', label: 'Warehouse' },
      { key: 'productCount', label: 'Product Count', type: 'number' },
      { key: 'totalQty', label: 'Total Qty', type: 'number' },
      { key: 'stockValue', label: 'Stock Value', type: 'currency' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindStockTransfers(report: StockTransferReport): void {
    this.summaryCards = [
      { label: 'Transfers', value: report.totalTransfers, type: 'number' },
      { label: 'Item Count', value: report.totalItems, type: 'number', accent: 'accent-blue' },
      { label: 'Total Quantity', value: report.totalQuantity, type: 'number', accent: 'accent-profit' }
    ];
    this.tableColumns = [
      { key: 'date', label: 'Date', type: 'date' },
      { key: 'transferNo', label: 'Transfer No' },
      { key: 'sourceWarehouse', label: 'Source Warehouse' },
      { key: 'destinationWarehouse', label: 'Destination Warehouse' },
      { key: 'status', label: 'Status' },
      { key: 'itemCount', label: 'Item Count', type: 'number' },
      { key: 'totalQty', label: 'Total Qty', type: 'number' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindCustomerDue(report: CustomerDueReport): void {
    this.summaryCards = [
      { label: 'Total Due', value: report.totalCustomerDue, type: 'currency', accent: 'accent-amber' },
      { label: 'Customers With Due', value: report.totalCustomersWithDue, type: 'number' }
    ];
    this.tableColumns = [
      { key: 'customer', label: 'Customer' },
      { key: 'totalSales', label: 'Total Sales', type: 'currency' },
      { key: 'paid', label: 'Paid', type: 'currency' },
      { key: 'due', label: 'Due', type: 'currency' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindSupplierDue(report: SupplierDueReport): void {
    this.summaryCards = [
      { label: 'Total Due', value: report.totalSupplierDue, type: 'currency', accent: 'accent-amber' },
      { label: 'Suppliers With Due', value: report.totalSuppliersWithDue, type: 'number' }
    ];
    this.tableColumns = [
      { key: 'supplier', label: 'Supplier' },
      { key: 'totalPurchase', label: 'Total Purchase', type: 'currency' },
      { key: 'paid', label: 'Paid', type: 'currency' },
      { key: 'due', label: 'Due', type: 'currency' }
    ];
    this.tableRows = this.asTableRows(report.rows);
  }

  private bindProfitLoss(report: ProfitLossSummary): void {
    this.summaryCards = [
      { label: 'Revenue', value: report.revenue, type: 'currency' },
      { label: 'Purchase Cost', value: report.purchaseCost, type: 'currency', accent: 'accent-blue' },
      { label: 'Gross Profit', value: report.grossProfit, type: 'currency', accent: 'accent-profit' },
      { label: 'Expense', value: report.expense, type: 'currency', accent: 'accent-neutral' },
      { label: 'Net Profit', value: report.netProfit, type: 'currency', accent: 'accent-amber' }
    ];
    this.tableColumns = [];
    this.tableRows = [];
    this.emptyMessage = 'Profit and loss summary loaded.';
  }

  private canSeeCard(card: ReportCard): boolean {
    if (card.permissions?.length) {
      return card.permissions.every(permission => this.hasPermission(permission));
    }
    if (card.anyPermissions?.length) {
      return card.anyPermissions.some(permission => this.hasPermission(permission));
    }
    return true;
  }

  private normalizeReportType(value: string): DetailReportType {
    const allowed: DetailReportType[] = [
      'sales-summary',
      'sales-detail',
      'top-selling-products',
      'customer-sales',
      'purchase-summary',
      'purchase-detail',
      'supplier-purchases',
      'purchase-returns',
      'stock',
      'stock-movements',
      'low-stock',
      'warehouse-stock-valuation',
      'stock-transfers',
      'customer-dues',
      'supplier-dues',
      'profit-loss'
    ];
    return allowed.includes(value as DetailReportType) ? (value as DetailReportType) : 'sales-summary';
  }

  private resetDetailState(): void {
    this.filters = {};
    this.summaryCards = [];
    this.tableColumns = [];
    this.tableRows = [];
    this.emptyMessage = 'No data found for the selected filters.';
    this.error = '';
  }

  private csv(): string {
    const rows = this.tableRows.map(row => this.tableColumns.map(column => this.formatCell(row, column)));
    const header = this.tableColumns.map(column => column.label);
    return [header, ...rows]
      .map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(','))
      .join('\r\n');
  }

  private download(content: string, filename: string, type: string): void {
    const url = URL.createObjectURL(new Blob([content], { type }));
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  private asTableRows<T extends object>(rows: T[]): Array<Record<string, unknown>> {
    return rows.map(row => ({ ...(row as object) })) as Array<Record<string, unknown>>;
  }
}
