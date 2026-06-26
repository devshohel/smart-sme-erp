import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';

export interface ReportFilters {
  fromDate?: string;
  toDate?: string;
  startDate?: string;
  endDate?: string;
  status?: string;
  keyword?: string;
  customerId?: number | null;
  supplierId?: number | null;
  productId?: number | null;
  warehouseId?: number | null;
  categoryId?: number | null;
  brandId?: number | null;
}

export type ReportExportFormat = 'csv' | 'excel' | 'pdf';

export interface SalesReport {
  totalSales: number;
  totalPaid: number;
  totalDue: number;
  totalInvoices: number;
  returnAmount: number;
  netSales: number;
  rows: SalesReportRow[];
}

export interface SalesReportRow {
  invoiceNo: string;
  customer: string;
  warehouse: string;
  status: string;
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
  returnAmount: number;
  netPurchase: number;
  rows: PurchaseReportRow[];
}

export interface PurchaseReportRow {
  poNo: string;
  supplier: string;
  warehouse: string;
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
  sku: string;
  category: string;
  brand: string;
  warehouse: string;
  quantity: number;
  reorderLevel: number;
  status: string;
  stockValue: number;
}

export interface StockMovementReportRow {
  date: string;
  product: string;
  warehouse: string;
  movementType: string;
  quantity: number;
  quantityBefore: number;
  quantityChange: number;
  quantityAfter: number;
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

export interface TopSellingProductReport {
  totalQuantitySold: number;
  totalGrossSales: number;
  totalReturnQty: number;
  totalNetQty: number;
  rows: TopSellingProductRow[];
}

export interface TopSellingProductRow {
  productId?: number;
  product: string;
  sku: string;
  quantitySold: number;
  grossSales: number;
  returnQty: number;
  netQty: number;
}

export interface CustomerSalesReport {
  totalCustomers: number;
  totalSales: number;
  totalPaid: number;
  totalDue: number;
  rows: CustomerSalesRow[];
}

export interface CustomerSalesRow {
  customerId?: number;
  customer: string;
  invoiceCount: number;
  totalSales: number;
  paidAmount: number;
  dueAmount: number;
  lastSaleDate: string;
}

export interface SupplierPurchaseReport {
  totalSuppliers: number;
  totalPurchase: number;
  totalPaid: number;
  totalDue: number;
  rows: SupplierPurchaseRow[];
}

export interface SupplierPurchaseRow {
  supplierId?: number;
  supplier: string;
  purchaseCount: number;
  totalPurchase: number;
  paidAmount: number;
  dueAmount: number;
  lastPurchaseDate: string;
}

export interface PurchaseReturnReport {
  returnCount: number;
  totalReturnAmount: number;
  rows: PurchaseReturnRow[];
}

export interface SalesReturnReport {
  returnCount: number;
  totalReturnAmount: number;
  rows: SalesReturnRow[];
}

export interface SalesReturnRow {
  returnNo: string;
  customer: string;
  invoiceNo: string;
  date: string;
  quantity: number;
  amount: number;
  status: string;
}

export interface PurchaseByProductReport {
  totalQuantityPurchased: number;
  totalGrossPurchase: number;
  totalReturnQty: number;
  totalNetQty: number;
  rows: PurchaseByProductRow[];
}

export interface PurchaseByProductRow {
  productId?: number;
  product: string;
  sku: string;
  quantityPurchased: number;
  grossPurchase: number;
  returnQty: number;
  netQty: number;
}

export interface PurchaseReturnRow {
  returnNo: string;
  supplier: string;
  purchaseNo: string;
  date: string;
  amount: number;
  status: string;
}

export interface LowStockReport {
  totalLowStockItems: number;
  totalShortageQty: number;
  rows: LowStockRow[];
}

export interface LowStockRow {
  product: string;
  sku: string;
  warehouse: string;
  currentQty: number;
  reorderLevel: number;
  shortageQty: number;
}

export interface WarehouseStockValuationReport {
  totalWarehouses: number;
  totalQuantity: number;
  totalStockValue: number;
  rows: WarehouseStockValuationRow[];
}

export interface WarehouseStockValuationRow {
  warehouse: string;
  productCount: number;
  totalQty: number;
  stockValue: number;
}

export interface StockTransferReport {
  totalTransfers: number;
  totalItems: number;
  totalQuantity: number;
  rows: StockTransferRow[];
}

export interface StockTransferRow {
  transferNo: string;
  sourceWarehouse: string;
  destinationWarehouse: string;
  status: string;
  date: string;
  itemCount: number;
  totalQty: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportsService {
  private readonly baseUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getSalesSummary(filters: ReportFilters): Observable<SalesReport> {
    return this.http
      .get<SalesReport | ApiResponse<SalesReport>>(`${this.baseUrl}/sales-summary`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeSales(unwrapApiResponse(response))));
  }

  getSalesDetail(filters: ReportFilters): Observable<SalesReport> {
    return this.http
      .get<SalesReport | ApiResponse<SalesReport>>(`${this.baseUrl}/sales-detail`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeSales(unwrapApiResponse(response))));
  }

  getTopSellingProducts(filters: ReportFilters): Observable<TopSellingProductReport> {
    return this.http
      .get<TopSellingProductReport | ApiResponse<TopSellingProductReport>>(`${this.baseUrl}/top-selling-products`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeTopSelling(unwrapApiResponse(response))));
  }

  getCustomerSales(filters: ReportFilters): Observable<CustomerSalesReport> {
    return this.http
      .get<CustomerSalesReport | ApiResponse<CustomerSalesReport>>(`${this.baseUrl}/customer-sales`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeCustomerSales(unwrapApiResponse(response))));
  }

  getSalesReturns(filters: ReportFilters): Observable<SalesReturnReport> {
    return this.http
      .get<SalesReturnReport | ApiResponse<SalesReturnReport>>(`${this.baseUrl}/sales-returns`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeSalesReturns(unwrapApiResponse(response))));
  }

  getPurchaseSummary(filters: ReportFilters): Observable<PurchaseReport> {
    return this.http
      .get<PurchaseReport | ApiResponse<PurchaseReport>>(`${this.baseUrl}/purchase-summary`, { params: this.params(filters) })
      .pipe(map(response => this.normalizePurchase(unwrapApiResponse(response))));
  }

  getPurchaseDetail(filters: ReportFilters): Observable<PurchaseReport> {
    return this.http
      .get<PurchaseReport | ApiResponse<PurchaseReport>>(`${this.baseUrl}/purchase-detail`, { params: this.params(filters) })
      .pipe(map(response => this.normalizePurchase(unwrapApiResponse(response))));
  }

  getSupplierPurchases(filters: ReportFilters): Observable<SupplierPurchaseReport> {
    return this.http
      .get<SupplierPurchaseReport | ApiResponse<SupplierPurchaseReport>>(`${this.baseUrl}/supplier-purchases`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeSupplierPurchases(unwrapApiResponse(response))));
  }

  getPurchaseByProduct(filters: ReportFilters): Observable<PurchaseByProductReport> {
    return this.http
      .get<PurchaseByProductReport | ApiResponse<PurchaseByProductReport>>(`${this.baseUrl}/purchase-by-product`, { params: this.params(filters) })
      .pipe(map(response => this.normalizePurchaseByProduct(unwrapApiResponse(response))));
  }

  getPurchaseReturns(filters: ReportFilters): Observable<PurchaseReturnReport> {
    return this.http
      .get<PurchaseReturnReport | ApiResponse<PurchaseReturnReport>>(`${this.baseUrl}/purchase-returns`, { params: this.params(filters) })
      .pipe(map(response => this.normalizePurchaseReturns(unwrapApiResponse(response))));
  }

  getStockReport(filters: ReportFilters): Observable<StockReport> {
    return this.http
      .get<StockReport | ApiResponse<StockReport>>(`${this.baseUrl}/stock`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeStock(unwrapApiResponse(response))));
  }

  getStockMovements(filters: ReportFilters): Observable<StockReport> {
    return this.http
      .get<StockReport | ApiResponse<StockReport>>(`${this.baseUrl}/stock-movements`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeStock(unwrapApiResponse(response))));
  }

  getNegativeStock(filters: ReportFilters): Observable<StockReport> {
    return this.http
      .get<StockReport | ApiResponse<StockReport>>(`${this.baseUrl}/negative-stock`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeStock(unwrapApiResponse(response))));
  }

  getLowStock(filters: ReportFilters): Observable<LowStockReport> {
    return this.http
      .get<LowStockReport | ApiResponse<LowStockReport>>(`${this.baseUrl}/low-stock`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeLowStock(unwrapApiResponse(response))));
  }

  getWarehouseStockValuation(filters: ReportFilters): Observable<WarehouseStockValuationReport> {
    return this.http
      .get<WarehouseStockValuationReport | ApiResponse<WarehouseStockValuationReport>>(`${this.baseUrl}/warehouse-stock-valuation`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeWarehouseValuation(unwrapApiResponse(response))));
  }

  getStockTransfers(filters: ReportFilters): Observable<StockTransferReport> {
    return this.http
      .get<StockTransferReport | ApiResponse<StockTransferReport>>(`${this.baseUrl}/stock-transfers`, { params: this.params(filters) })
      .pipe(map(response => this.normalizeStockTransfers(unwrapApiResponse(response))));
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

  exportReport(report: string, filters: ReportFilters, format: ReportExportFormat): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      params: this.params({ ...filters }).set('report', report).set('format', format),
      responseType: 'blob'
    });
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
      returnAmount: this.toNumber(report.returnAmount),
      netSales: this.toNumber(report.netSales),
      rows: this.toArray<SalesReportRow>(report.rows).map(row => ({
        invoiceNo: row.invoiceNo || '',
        customer: row.customer || '',
        warehouse: row.warehouse || '',
        status: row.status || '',
        date: row.date || '',
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
      returnAmount: this.toNumber(report.returnAmount),
      netPurchase: this.toNumber(report.netPurchase),
      rows: this.toArray<PurchaseReportRow>(report.rows).map(row => ({
        poNo: row.poNo || '',
        supplier: row.supplier || '',
        warehouse: row.warehouse || '',
        date: row.date || '',
        amount: this.toNumber(row.amount),
        paid: this.toNumber(row.paid),
        due: this.toNumber(row.due),
        status: row.status || ''
      }))
    };
  }

  private normalizeStock(report: Partial<StockReport>): StockReport {
    return {
      totalStockQuantity: this.toNumber(report.totalStockQuantity),
      totalStockValue: this.toNumber(report.totalStockValue),
      lowStockCount: this.toNumber(report.lowStockCount),
      rows: this.toArray<StockReportRow>(report.rows).map(row => ({
        product: row.product || '',
        sku: row.sku || '',
        category: row.category || '',
        brand: row.brand || '',
        warehouse: row.warehouse || '',
        quantity: this.toNumber(row.quantity),
        reorderLevel: this.toNumber(row.reorderLevel),
        status: row.status || '',
        stockValue: this.toNumber(row.stockValue)
      })),
      movements: this.toArray<StockMovementReportRow>(report.movements).map(row => ({
        date: row.date || '',
        product: row.product || '',
        warehouse: row.warehouse || '',
        movementType: row.movementType || '',
        quantity: this.toNumber(row.quantity),
        quantityBefore: this.toNumber(row.quantityBefore),
        quantityChange: this.toNumber(row.quantityChange),
        quantityAfter: this.toNumber(row.quantityAfter),
        referenceNo: row.referenceNo || ''
      }))
    };
  }

  private normalizeCustomerDue(report: Partial<CustomerDueReport>): CustomerDueReport {
    return {
      totalCustomerDue: this.toNumber(report.totalCustomerDue),
      totalCustomersWithDue: this.toNumber(report.totalCustomersWithDue),
      rows: this.toArray<CustomerDueReportRow>(report.rows).map(row => ({
        customer: row.customer || '',
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
        supplier: row.supplier || '',
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

  private normalizeTopSelling(report: Partial<TopSellingProductReport>): TopSellingProductReport {
    return {
      totalQuantitySold: this.toNumber(report.totalQuantitySold),
      totalGrossSales: this.toNumber(report.totalGrossSales),
      totalReturnQty: this.toNumber(report.totalReturnQty),
      totalNetQty: this.toNumber(report.totalNetQty),
      rows: this.toArray<TopSellingProductRow>(report.rows).map(row => ({
        productId: row.productId,
        product: row.product || '',
        sku: row.sku || '',
        quantitySold: this.toNumber(row.quantitySold),
        grossSales: this.toNumber(row.grossSales),
        returnQty: this.toNumber(row.returnQty),
        netQty: this.toNumber(row.netQty)
      }))
    };
  }

  private normalizeCustomerSales(report: Partial<CustomerSalesReport>): CustomerSalesReport {
    return {
      totalCustomers: this.toNumber(report.totalCustomers),
      totalSales: this.toNumber(report.totalSales),
      totalPaid: this.toNumber(report.totalPaid),
      totalDue: this.toNumber(report.totalDue),
      rows: this.toArray<CustomerSalesRow>(report.rows).map(row => ({
        customerId: row.customerId,
        customer: row.customer || '',
        invoiceCount: this.toNumber(row.invoiceCount),
        totalSales: this.toNumber(row.totalSales),
        paidAmount: this.toNumber(row.paidAmount),
        dueAmount: this.toNumber(row.dueAmount),
        lastSaleDate: row.lastSaleDate || ''
      }))
    };
  }

  private normalizeSupplierPurchases(report: Partial<SupplierPurchaseReport>): SupplierPurchaseReport {
    return {
      totalSuppliers: this.toNumber(report.totalSuppliers),
      totalPurchase: this.toNumber(report.totalPurchase),
      totalPaid: this.toNumber(report.totalPaid),
      totalDue: this.toNumber(report.totalDue),
      rows: this.toArray<SupplierPurchaseRow>(report.rows).map(row => ({
        supplierId: row.supplierId,
        supplier: row.supplier || '',
        purchaseCount: this.toNumber(row.purchaseCount),
        totalPurchase: this.toNumber(row.totalPurchase),
        paidAmount: this.toNumber(row.paidAmount),
        dueAmount: this.toNumber(row.dueAmount),
        lastPurchaseDate: row.lastPurchaseDate || ''
      }))
    };
  }

  private normalizePurchaseReturns(report: Partial<PurchaseReturnReport>): PurchaseReturnReport {
    return {
      returnCount: this.toNumber(report.returnCount),
      totalReturnAmount: this.toNumber(report.totalReturnAmount),
      rows: this.toArray<PurchaseReturnRow>(report.rows).map(row => ({
        returnNo: row.returnNo || '',
        supplier: row.supplier || '',
        purchaseNo: row.purchaseNo || '',
        date: row.date || '',
        amount: this.toNumber(row.amount),
        status: row.status || ''
      }))
    };
  }

  private normalizeSalesReturns(report: Partial<SalesReturnReport>): SalesReturnReport {
    return {
      returnCount: this.toNumber(report.returnCount),
      totalReturnAmount: this.toNumber(report.totalReturnAmount),
      rows: this.toArray<SalesReturnRow>(report.rows).map(row => ({
        returnNo: row.returnNo || '',
        customer: row.customer || '',
        invoiceNo: row.invoiceNo || '',
        date: row.date || '',
        quantity: this.toNumber(row.quantity),
        amount: this.toNumber(row.amount),
        status: row.status || ''
      }))
    };
  }

  private normalizePurchaseByProduct(report: Partial<PurchaseByProductReport>): PurchaseByProductReport {
    return {
      totalQuantityPurchased: this.toNumber(report.totalQuantityPurchased),
      totalGrossPurchase: this.toNumber(report.totalGrossPurchase),
      totalReturnQty: this.toNumber(report.totalReturnQty),
      totalNetQty: this.toNumber(report.totalNetQty),
      rows: this.toArray<PurchaseByProductRow>(report.rows).map(row => ({
        productId: row.productId,
        product: row.product || '',
        sku: row.sku || '',
        quantityPurchased: this.toNumber(row.quantityPurchased),
        grossPurchase: this.toNumber(row.grossPurchase),
        returnQty: this.toNumber(row.returnQty),
        netQty: this.toNumber(row.netQty)
      }))
    };
  }

  private normalizeLowStock(report: Partial<LowStockReport>): LowStockReport {
    return {
      totalLowStockItems: this.toNumber(report.totalLowStockItems),
      totalShortageQty: this.toNumber(report.totalShortageQty),
      rows: this.toArray<LowStockRow>(report.rows).map(row => ({
        product: row.product || '',
        sku: row.sku || '',
        warehouse: row.warehouse || '',
        currentQty: this.toNumber(row.currentQty),
        reorderLevel: this.toNumber(row.reorderLevel),
        shortageQty: this.toNumber(row.shortageQty)
      }))
    };
  }

  private normalizeWarehouseValuation(report: Partial<WarehouseStockValuationReport>): WarehouseStockValuationReport {
    return {
      totalWarehouses: this.toNumber(report.totalWarehouses),
      totalQuantity: this.toNumber(report.totalQuantity),
      totalStockValue: this.toNumber(report.totalStockValue),
      rows: this.toArray<WarehouseStockValuationRow>(report.rows).map(row => ({
        warehouse: row.warehouse || '',
        productCount: this.toNumber(row.productCount),
        totalQty: this.toNumber(row.totalQty),
        stockValue: this.toNumber(row.stockValue)
      }))
    };
  }

  private normalizeStockTransfers(report: Partial<StockTransferReport>): StockTransferReport {
    return {
      totalTransfers: this.toNumber(report.totalTransfers),
      totalItems: this.toNumber(report.totalItems),
      totalQuantity: this.toNumber(report.totalQuantity),
      rows: this.toArray<StockTransferRow>(report.rows).map(row => ({
        transferNo: row.transferNo || '',
        sourceWarehouse: row.sourceWarehouse || '',
        destinationWarehouse: row.destinationWarehouse || '',
        status: row.status || '',
        date: row.date || '',
        itemCount: this.toNumber(row.itemCount),
        totalQty: this.toNumber(row.totalQty)
      }))
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
