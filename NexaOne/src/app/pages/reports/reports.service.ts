import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';

export interface ReportFilters {
  startDate?: string;
  endDate?: string;
  customerId?: number | null;
  supplierId?: number | null;
  productId?: number | null;
  warehouseId?: number | null;
}

export interface SalesReport {
  totalSales: number;
  totalPaid: number;
  totalDue: number;
  totalInvoices: number;
  rows: SalesReportRow[];
}

export interface SalesReportRow {
  invoiceNo: string;
  customer: string;
  date: string;
  quantity: number;
  amount: number;
  paid: number;
  due: number;
}

export interface PurchaseReport {
  totalPurchase: number;
  totalPaid: number;
  totalDue: number;
  totalOrders: number;
  rows: PurchaseReportRow[];
}

export interface PurchaseReportRow {
  poNo: string;
  supplier: string;
  date: string;
  amount: number;
  paid: number;
  due: number;
  status: string;
}

export interface StockReport {
  totalStockQuantity: number;
  totalStockValue: number;
  lowStockCount: number;
  rows: StockReportRow[];
  movements: StockMovementReportRow[];
}

export interface StockReportRow {
  product: string;
  warehouse: string;
  quantity: number;
  reorderLevel: number;
  stockValue: number;
}

export interface StockMovementReportRow {
  date: string;
  product: string;
  movementType: string;
  quantity: number;
  referenceNo: string;
}

export interface CustomerDueReport {
  totalCustomerDue: number;
  totalCustomersWithDue: number;
  rows: CustomerDueReportRow[];
}

export interface CustomerDueReportRow {
  customer: string;
  totalSales: number;
  paid: number;
  due: number;
}

export interface SupplierDueReport {
  totalSupplierDue: number;
  totalSuppliersWithDue: number;
  rows: SupplierDueReportRow[];
}

export interface SupplierDueReportRow {
  supplier: string;
  totalPurchase: number;
  paid: number;
  due: number;
}

export interface ProfitLossSummary {
  revenue: number;
  purchaseCost: number;
  grossProfit: number;
  expense: number;
  netProfit: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportsService {
  private readonly baseUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getSalesReport(filters: ReportFilters): Observable<SalesReport> {
    return this.http
      .get<SalesReport | ApiResponse<SalesReport>>(`${this.baseUrl}/sales`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeSales(unwrapApiResponse(response))));
  }

  getPurchaseReport(filters: ReportFilters): Observable<PurchaseReport> {
    return this.http
      .get<PurchaseReport | ApiResponse<PurchaseReport>>(`${this.baseUrl}/purchases`, { params: this.params(filters) })
      .pipe(map(response => this.normalizePurchase(unwrapApiResponse(response))));
  }

  getStockReport(filters: ReportFilters): Observable<StockReport> {
    return this.http
      .get<StockReport | ApiResponse<StockReport>>(`${this.baseUrl}/stock`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeStock(unwrapApiResponse(response))));
  }

  getCustomerDueReport(): Observable<CustomerDueReport> {
    return this.http
      .get<CustomerDueReport | ApiResponse<CustomerDueReport>>(`${this.baseUrl}/customer-dues`)
      .pipe(map(response => this.normalizeCustomerDue(unwrapApiResponse(response))));
  }

  getSupplierDueReport(): Observable<SupplierDueReport> {
    return this.http
      .get<SupplierDueReport | ApiResponse<SupplierDueReport>>(`${this.baseUrl}/supplier-dues`)
      .pipe(map(response => this.normalizeSupplierDue(unwrapApiResponse(response))));
  }

  getProfitLossSummary(filters: ReportFilters): Observable<ProfitLossSummary> {
    return this.http
      .get<ProfitLossSummary | ApiResponse<ProfitLossSummary>>(`${this.baseUrl}/profit-loss`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeProfitLoss(unwrapApiResponse(response))));
  }

  private params(filters: ReportFilters): HttpParams {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }

  private normalizeSales(report: Partial<SalesReport>): SalesReport {
    return {
      totalSales: this.toNumber(report.totalSales),
      totalPaid: this.toNumber(report.totalPaid),
      totalDue: this.toNumber(report.totalDue),
      totalInvoices: this.toNumber(report.totalInvoices),
      rows: this.toArray<SalesReportRow>(report.rows).map(row => ({
        ...row,
        quantity: this.toNumber(row.quantity),
        amount: this.toNumber(row.amount),
        paid: this.toNumber(row.paid),
        due: this.toNumber(row.due)
      }))
    };
  }

  private normalizePurchase(report: Partial<PurchaseReport>): PurchaseReport {
    return {
      totalPurchase: this.toNumber(report.totalPurchase),
      totalPaid: this.toNumber(report.totalPaid),
      totalDue: this.toNumber(report.totalDue),
      totalOrders: this.toNumber(report.totalOrders),
      rows: this.toArray<PurchaseReportRow>(report.rows).map(row => ({
        ...row,
        amount: this.toNumber(row.amount),
        paid: this.toNumber(row.paid),
        due: this.toNumber(row.due)
      }))
    };
  }

  private normalizeStock(report: Partial<StockReport>): StockReport {
    return {
      totalStockQuantity: this.toNumber(report.totalStockQuantity),
      totalStockValue: this.toNumber(report.totalStockValue),
      lowStockCount: this.toNumber(report.lowStockCount),
      rows: this.toArray<StockReportRow>(report.rows).map(row => ({
        ...row,
        quantity: this.toNumber(row.quantity),
        reorderLevel: this.toNumber(row.reorderLevel),
        stockValue: this.toNumber(row.stockValue)
      })),
      movements: this.toArray<StockMovementReportRow>(report.movements).map(row => ({
        ...row,
        quantity: this.toNumber(row.quantity)
      }))
    };
  }

  private normalizeCustomerDue(report: Partial<CustomerDueReport>): CustomerDueReport {
    return {
      totalCustomerDue: this.toNumber(report.totalCustomerDue),
      totalCustomersWithDue: this.toNumber(report.totalCustomersWithDue),
      rows: this.toArray<CustomerDueReportRow>(report.rows).map(row => ({
        ...row,
        totalSales: this.toNumber(row.totalSales),
        paid: this.toNumber(row.paid),
        due: this.toNumber(row.due)
      }))
    };
  }

  private normalizeSupplierDue(report: Partial<SupplierDueReport>): SupplierDueReport {
    return {
      totalSupplierDue: this.toNumber(report.totalSupplierDue),
      totalSuppliersWithDue: this.toNumber(report.totalSuppliersWithDue),
      rows: this.toArray<SupplierDueReportRow>(report.rows).map(row => ({
        ...row,
        totalPurchase: this.toNumber(row.totalPurchase),
        paid: this.toNumber(row.paid),
        due: this.toNumber(row.due)
      }))
    };
  }

  private normalizeProfitLoss(report: Partial<ProfitLossSummary>): ProfitLossSummary {
    return {
      revenue: this.toNumber(report.revenue),
      purchaseCost: this.toNumber(report.purchaseCost),
      grossProfit: this.toNumber(report.grossProfit),
      expense: this.toNumber(report.expense),
      netProfit: this.toNumber(report.netProfit)
    };
  }

  private toArray<T>(value: unknown): T[] {
    return Array.isArray(value) ? value : [];
  }

  private toNumber(value: unknown): number {
    const numberValue = Number(value ?? 0);
    return Number.isFinite(numberValue) ? numberValue : 0;
  }
}
