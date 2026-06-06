import { Component, OnInit } from '@angular/core';
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

  constructor(private dashboardService: DashboardService) { }

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
    const rows = this.summary?.monthlySalesPurchase || [];
    const max = rows.reduce((value, item) => Math.max(value, item.sales, item.purchase), 0);
    return max > 0 ? max : 1;
  }

  barHeight(value: number): number {
    return Math.max(4, Math.round((Number(value || 0) / this.maxMonthlyValue()) * 120));
  }

  profitTrendRows(): MonthlySalesPurchase[] {
    return this.summary?.monthlySalesPurchase || [];
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
