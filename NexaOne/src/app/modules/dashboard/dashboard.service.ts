import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/utils/api-response.util';
import {
  DashboardChartPoint,
  DashboardFilter,
  DashboardSalesPurchasePoint,
  DashboardSummary,
  DueAlert,
  LowStockAlert,
  PendingApproval,
  RecentDocument,
  RecentTransaction,
  TopSellingProduct
} from './dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly summaryUrl = `${environment.apiUrl}/dashboard/summary`;
  private readonly latestSummarySubject = new BehaviorSubject<DashboardSummary | null>(null);
  latestSummary$ = this.latestSummarySubject.asObservable();

  constructor(private http: HttpClient) {}

  getSummary(filter: DashboardFilter = { period: 'month' }): Observable<DashboardSummary> {
    let params = new HttpParams().set('period', filter.period);
    if (filter.fromDate) {
      params = params.set('fromDate', filter.fromDate);
    }
    if (filter.toDate) {
      params = params.set('toDate', filter.toDate);
    }

    return this.http
      .get<DashboardSummary | ApiResponse<DashboardSummary>>(this.summaryUrl, { params })
      .pipe(
        map(response => this.normalizeSummary(this.unwrapDashboardResponse(response), filter.period)),
        tap(summary => this.latestSummarySubject.next(summary))
      );
  }

  private unwrapDashboardResponse(response: unknown): Partial<DashboardSummary> {
    if (!response || typeof response !== 'object') {
      return {};
    }

    const responseObject = response as Record<string, unknown>;
    const data = responseObject['data'];
    if (data && typeof data === 'object') {
      return data as Partial<DashboardSummary>;
    }

    return responseObject as Partial<DashboardSummary>;
  }

  private normalizeSummary(summary: Partial<DashboardSummary>, fallbackPeriod: DashboardFilter['period']): DashboardSummary {
    return {
      period: (summary.period as DashboardSummary['period']) || fallbackPeriod,
      fromDate: String(summary.fromDate || ''),
      toDate: String(summary.toDate || ''),
      generatedAt: String(summary.generatedAt || ''),
      periodSales: this.toNumber(summary.periodSales),
      periodPurchase: this.toNumber(summary.periodPurchase),
      periodExpense: this.toNumber(summary.periodExpense),
      netProfit: this.toNumber(summary.netProfit),
      totalStockValue: this.toNumber(summary.totalStockValue),
      customerReceivable: this.toNumber(summary.customerReceivable ?? summary.customerDue),
      supplierPayable: this.toNumber(summary.supplierPayable ?? summary.supplierDue),
      cashBankBalance: this.toNumber(summary.cashBankBalance),
      lowStockItemsCount: this.toNumber(summary.lowStockItemsCount),
      pendingApprovalsCount: this.toNumber(summary.pendingApprovalsCount),
      trialBalanceDifference: this.toNumber(summary.trialBalanceDifference),
      budgetUtilization: this.toNumber(summary.budgetUtilization),
      todaySales: this.toNumber(summary.todaySales),
      todayPurchase: this.toNumber(summary.todayPurchase),
      todayExpense: this.toNumber(summary.todayExpense),
      todayProfit: this.toNumber(summary.todayProfit),
      customerDue: this.toNumber(summary.customerDue ?? summary.customerReceivable),
      supplierDue: this.toNumber(summary.supplierDue ?? summary.supplierPayable),
      pendingApprovalExpenses: this.toNumber(summary.pendingApprovalExpenses),
      monthlyIncomeExpense: this.toChartPoints(summary.monthlyIncomeExpense),
      expenseByCategory: this.toChartPoints(summary.expenseByCategory),
      warehouseStockValue: this.toChartPoints(summary.warehouseStockValue),
      cashBankTrend: this.toChartPoints(summary.cashBankTrend),
      salesPurchaseTrend: this.toSalesPurchase(summary.salesPurchaseTrend ?? summary.monthlySalesPurchase),
      monthlySalesPurchase: this.toSalesPurchase(summary.monthlySalesPurchase ?? summary.salesPurchaseTrend),
      topSellingProducts: this.toTopSellingProducts(summary.topSellingProducts),
      lowStockAlerts: this.toLowStockAlerts(summary.lowStockAlerts),
      dueAlerts: this.toDueAlerts(summary.dueAlerts),
      recentTransactions: this.toRecentTransactions(summary.recentTransactions),
      pendingApprovals: this.toPendingApprovals(summary.pendingApprovals),
      recentSales: this.toRecentDocuments(summary.recentSales),
      recentPurchases: this.toRecentDocuments(summary.recentPurchases)
    };
  }

  private toChartPoints(value: unknown): DashboardChartPoint[] {
    return this.toArray<any>(value).map(item => ({
      label: String(item?.label || ''),
      value: this.toNumber(item?.value),
      secondaryValue: this.toNumber(item?.secondaryValue)
    }));
  }

  private toSalesPurchase(value: unknown): DashboardSalesPurchasePoint[] {
    return this.toArray<any>(value).map(item => ({
      label: String(item?.label || ''),
      sales: this.toNumber(item?.sales),
      purchase: this.toNumber(item?.purchase)
    }));
  }

  private toTopSellingProducts(value: unknown): TopSellingProduct[] {
    return this.toArray<any>(value).map(item => ({
      productId: item?.productId ?? null,
      productName: String(item?.productName || ''),
      sku: item?.sku ?? null,
      quantity: this.toNumber(item?.quantity),
      amount: this.toNumber(item?.amount)
    }));
  }

  private toLowStockAlerts(value: unknown): LowStockAlert[] {
    return this.toArray<any>(value).map(item => ({
      productId: item?.productId ?? null,
      productName: String(item?.productName || ''),
      sku: item?.sku ?? null,
      warehouseName: String(item?.warehouseName || ''),
      quantity: this.toNumber(item?.quantity),
      reorderLevel: this.toNumber(item?.reorderLevel)
    }));
  }

  private toDueAlerts(value: unknown): DueAlert[] {
    return this.toArray<any>(value).map(item => ({
      type: String(item?.type || ''),
      referenceNo: String(item?.referenceNo || ''),
      partyName: String(item?.partyName || ''),
      dueAmount: this.toNumber(item?.dueAmount),
      date: String(item?.date || '')
    }));
  }

  private toRecentTransactions(value: unknown): RecentTransaction[] {
    return this.toArray<any>(value).map(item => ({
      type: String(item?.type || ''),
      referenceNo: String(item?.referenceNo || ''),
      partyName: String(item?.partyName || ''),
      description: item?.description ?? null,
      amount: this.toNumber(item?.amount),
      status: String(item?.status || ''),
      date: String(item?.date || '')
    }));
  }

  private toPendingApprovals(value: unknown): PendingApproval[] {
    return this.toArray<any>(value).map(item => ({
      type: String(item?.type || ''),
      referenceNo: String(item?.referenceNo || ''),
      partyName: String(item?.partyName || ''),
      amount: this.toNumber(item?.amount),
      status: String(item?.status || ''),
      date: String(item?.date || '')
    }));
  }

  private toRecentDocuments(value: unknown): RecentDocument[] {
    return this.toArray<any>(value).map(item => ({
      referenceNo: String(item?.referenceNo || ''),
      partyName: String(item?.partyName || ''),
      amount: this.toNumber(item?.amount),
      status: String(item?.status || ''),
      date: String(item?.date || '')
    }));
  }

  private toArray<T>(value: unknown): T[] {
    return Array.isArray(value) ? value : [];
  }

  private toNumber(value: unknown): number {
    const numberValue = Number(value ?? 0);
    return Number.isFinite(numberValue) ? numberValue : 0;
  }
}
