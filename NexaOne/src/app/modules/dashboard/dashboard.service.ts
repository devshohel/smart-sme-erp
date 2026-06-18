import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  DashboardSummary,
  DueAlert,
  LowStockAlert,
  MonthlySalesPurchase,
  RecentTransaction,
  TopSellingProduct
} from './dashboard.model';
import { ApiResponse } from '../../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly summaryUrl = `${environment.apiUrl}/dashboard/summary`;
  private readonly latestSummarySubject = new BehaviorSubject<DashboardSummary | null>(null);
  latestSummary$ = this.latestSummarySubject.asObservable();

  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    return this.http
      .get<DashboardSummary | ApiResponse<DashboardSummary>>(this.summaryUrl)
      .pipe(
        map(response => this.normalizeSummary(this.unwrapDashboardResponse(response))),
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

  private normalizeSummary(summary: Partial<DashboardSummary>): DashboardSummary {
    return {
      ...summary,
      todaySales: this.toNumber(summary.todaySales),
      todayPurchase: this.toNumber(summary.todayPurchase),
      todayExpense: this.toNumber(summary.todayExpense),
      todayProfit: this.toNumber(summary.todayProfit),
      totalStockValue: this.toNumber(summary.totalStockValue),
      customerDue: this.toNumber(summary.customerDue),
      supplierDue: this.toNumber(summary.supplierDue),
      netProfit: this.toNumber(summary.netProfit),
      totalCustomers: this.toNumber(summary.totalCustomers),
      totalSuppliers: this.toNumber(summary.totalSuppliers),
      lowStockItemsCount: this.toNumber(summary.lowStockItemsCount),
      thisMonthProfit: this.toNumber(summary.thisMonthProfit),
      expenseTaxAmount: this.toNumber(summary.expenseTaxAmount),
      postedExpenses: this.toNumber(summary.postedExpenses),
      pendingApprovalExpenses: this.toNumber(summary.pendingApprovalExpenses),
      reversedExpenses: this.toNumber(summary.reversedExpenses),
      monthlyExpenseTrend: this.toArray<any>(summary.monthlyExpenseTrend).map(item => ({
        month: String(item?.month || ''),
        amount: this.toNumber(item?.amount),
        taxAmount: this.toNumber(item?.taxAmount)
      })),
      monthlySalesPurchase: this.toArray<MonthlySalesPurchase>(summary.monthlySalesPurchase).map(item => ({
        ...item,
        month: item.month || '',
        sales: this.toNumber(item.sales),
        purchase: this.toNumber(item.purchase),
        profit: this.toNumber(item.profit)
      })),
      topSellingProducts: this.toArray<TopSellingProduct>(summary.topSellingProducts).map(item => ({
        ...item,
        productId: item.productId ?? null,
        productName: item.productName || '',
        quantity: this.toNumber(item.quantity),
        amount: this.toNumber(item.amount)
      })),
      lowStockAlerts: this.toArray<LowStockAlert>(summary.lowStockAlerts).map(item => ({
        ...item,
        productId: item.productId ?? null,
        productName: item.productName || '',
        warehouseName: item.warehouseName || '',
        quantity: this.toNumber(item.quantity),
        reorderLevel: this.toNumber(item.reorderLevel)
      })),
      dueAlerts: this.toArray<DueAlert>(summary.dueAlerts).map(item => ({
        ...item,
        type: item.type || '',
        referenceNo: item.referenceNo || '',
        partyName: item.partyName || '',
        dueAmount: this.toNumber(item.dueAmount),
        date: item.date || ''
      })),
      recentTransactions: this.toArray<RecentTransaction>(summary.recentTransactions).map(item => ({
        ...item,
        type: item.type || '',
        referenceNo: item.referenceNo || '',
        partyName: item.partyName || '',
        amount: this.toNumber(item.amount),
        status: item.status || '',
        date: item.date || ''
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
