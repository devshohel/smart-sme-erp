import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardSummary, MonthlySalesPurchase } from '../dashboard.model';
import { DashboardService } from '../dashboard.service';
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary | null = null;
  loading = false;
  errorMessage = '';

  constructor(private dashboardService: DashboardService, private router: Router) { }

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.getSummary().subscribe({
      next: summary => {
        this.summary = summary;
        this.loading = false;
      },
      error: error => {
        this.summary = null;
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Dashboard data could not be loaded.');
      }
    });
  }

  maxMonthlyValue(): number {
    const rows = this.latestMonthlyRows();
    const max = rows.reduce((value, item) => Math.max(value, item.sales, item.purchase), 0);
    return max > 0 ? max : 1;
  }

  barHeight(value: number): number {
    return Math.max(4, Math.round((Number(value || 0) / this.maxMonthlyValue()) * 120));
  }

  maxTopProductAmount(): number {
    const rows = this.summary?.topSellingProducts || [];
    const max = rows.slice(0, 10).reduce((value, item) => Math.max(value, item.amount), 0);
    return max > 0 ? max : 1;
  }

  productBarWidth(amount: number): number {
    return Math.max(4, Math.round((Number(amount || 0) / this.maxTopProductAmount()) * 100));
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  latestMonthlyRows(): MonthlySalesPurchase[] {
    return (this.summary?.monthlySalesPurchase || []).slice(-4);
  }

  latestExpenseRows() {
    return (this.summary?.monthlyExpenseTrend || []).slice(-4);
  }

  monthLabel(value: string): string {
    if (!value) {
      return '';
    }
    const parsed = new Date(value);
    if (!Number.isNaN(parsed.getTime())) {
      return parsed.toLocaleString('en-US', { month: 'short' });
    }
    return value.length > 6 ? value.slice(0, 6) : value;
  }

  trackByMonth(index: number, item: MonthlySalesPurchase): string {
    return item.month;
  }

  statusClass(status: string): string {
    const normalized = (status || '').toUpperCase();
    if (['PAID', 'COMPLETED', 'CONFIRMED', 'RECEIVED'].includes(normalized)) {
      return 'bg-success-subtle text-success';
    }
    if (['PARTIAL', 'PARTIAL_PAID', 'APPROVED'].includes(normalized)) {
      return 'bg-info-subtle text-info';
    }
    if (['CANCELLED', 'DUE'].includes(normalized)) {
      return 'bg-danger-subtle text-danger';
    }
    return 'bg-warning-subtle text-warning';
  }
}
