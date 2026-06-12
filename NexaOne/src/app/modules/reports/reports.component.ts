import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { Customer } from '../../models/customer.model';
import { InventoryWarehouse } from '../../models/inventory-warehouse.model';
import { Product } from '../../models/product.model';
import { Supplier } from '../../models/supplier.model';
import { CustomerService } from '../../services/customer.service';
import { InventoryWarehouseService } from '../../services/inventory-warehouse.service';
import { ProductService } from '../../services/product.service';
import { SupplierService } from '../../services/supplier.service';
import {
  CustomerDueReport,
  ProfitLossSummary,
  PurchaseReport,
  ReportFilters,
  ReportsService,
  SalesReport,
  StockReport,
  SupplierDueReport
} from './reports.service';

type ReportType = 'sales' | 'purchases' | 'stock' | 'customer-dues' | 'supplier-dues' | 'profit-loss';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  reportType: ReportType = 'sales';
  stockTab: 'current' | 'movements' = 'current';
  loading = false;
  error = '';

  filters: ReportFilters = {};
  customers: Customer[] = [];
  suppliers: Supplier[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];

  salesReport: SalesReport | null = null;
  purchaseReport: PurchaseReport | null = null;
  stockReport: StockReport | null = null;
  customerDueReport: CustomerDueReport | null = null;
  supplierDueReport: SupplierDueReport | null = null;
  profitLossSummary: ProfitLossSummary | null = null;

  readonly reportTitles: Record<ReportType, string> = {
    sales: 'Sales Report',
    purchases: 'Purchase Report',
    stock: 'Stock Report',
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
    private warehouseService: InventoryWarehouseService
  ) { }

  ngOnInit(): void {
    this.loadFilterOptions();
    this.route.paramMap.subscribe(params => {
      this.reportType = this.normalizeReportType(params.get('type'));
      this.resetReportState();
      this.loadReport();
    });
  }

  setPeriod(period: 'today' | 'month' | 'custom'): void {
    if (period === 'custom') {
      return;
    }

    const today = new Date();
    if (period === 'today') {
      const value = this.toDateInput(today);
      this.filters.startDate = value;
      this.filters.endDate = value;
    } else {
      const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
      const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
      this.filters.startDate = this.toDateInput(firstDay);
      this.filters.endDate = this.toDateInput(lastDay);
    }

    this.loadReport();
  }

  loadReport(): void {
    this.loading = true;
    this.error = '';

    this.reportRequest().subscribe({
      next: report => {
        this.assignReport(report);
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load report data.';
        this.loading = false;
      }
    });
  }

  get title(): string {
    return this.reportTitles[this.reportType];
  }

  get showDateFilters(): boolean {
    return this.reportType === 'sales' || this.reportType === 'purchases' || this.reportType === 'profit-loss';
  }

  get hasRows(): boolean {
    if (this.reportType === 'sales') {
      return !!this.salesReport?.rows.length;
    }
    if (this.reportType === 'purchases') {
      return !!this.purchaseReport?.rows.length;
    }
    if (this.reportType === 'stock') {
      return this.stockTab === 'current'
        ? !!this.stockReport?.rows.length
        : !!this.stockReport?.movements.length;
    }
    if (this.reportType === 'customer-dues') {
      return !!this.customerDueReport?.rows.length;
    }
    if (this.reportType === 'supplier-dues') {
      return !!this.supplierDueReport?.rows.length;
    }
    return !!this.profitLossSummary;
  }

  private loadFilterOptions(): void {
    forkJoin({
      customers: this.customerService.getAllCustomers(),
      suppliers: this.supplierService.getAllSuppliers(),
      products: this.productService.getAllProducts(),
      warehouses: this.warehouseService.getAllWarehouses()
    }).subscribe({
      next: options => {
        this.customers = options.customers;
        this.suppliers = options.suppliers;
        this.products = options.products;
        this.warehouses = options.warehouses;
      }
    });
  }

  private reportRequest(): Observable<unknown> {
    if (this.reportType === 'sales') {
      return this.reportsService.getSalesReport(this.filters);
    }
    if (this.reportType === 'purchases') {
      return this.reportsService.getPurchaseReport(this.filters);
    }
    if (this.reportType === 'stock') {
      return this.reportsService.getStockReport(this.filters);
    }
    if (this.reportType === 'customer-dues') {
      return this.reportsService.getCustomerDueReport();
    }
    if (this.reportType === 'supplier-dues') {
      return this.reportsService.getSupplierDueReport();
    }
    return this.reportsService.getProfitLossSummary(this.filters);
  }

  private assignReport(report: unknown): void {
    if (this.reportType === 'sales') {
      this.salesReport = report as SalesReport;
    } else if (this.reportType === 'purchases') {
      this.purchaseReport = report as PurchaseReport;
    } else if (this.reportType === 'stock') {
      this.stockReport = report as StockReport;
    } else if (this.reportType === 'customer-dues') {
      this.customerDueReport = report as CustomerDueReport;
    } else if (this.reportType === 'supplier-dues') {
      this.supplierDueReport = report as SupplierDueReport;
    } else {
      this.profitLossSummary = report as ProfitLossSummary;
    }
  }

  private resetReportState(): void {
    this.salesReport = null;
    this.purchaseReport = null;
    this.stockReport = null;
    this.customerDueReport = null;
    this.supplierDueReport = null;
    this.profitLossSummary = null;
    this.stockTab = 'current';
    this.filters = {};
  }

  private normalizeReportType(value: string | null): ReportType {
    const allowed: ReportType[] = ['sales', 'purchases', 'stock', 'customer-dues', 'supplier-dues', 'profit-loss'];
    return allowed.includes(value as ReportType) ? value as ReportType : 'sales';
  }

  private toDateInput(date: Date): string {
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
