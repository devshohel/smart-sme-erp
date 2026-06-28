import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerAgingReport, CustomerAgingRow, CustomerOption } from '../../../models/customer.model';
import { CustomerService } from '../../../services/customer.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-customer-aging',
  templateUrl: './customer-aging.component.html',
  styleUrls: ['./customer-aging.component.css']
})
export class CustomerAgingComponent implements OnInit {
  report: CustomerAgingReport | null = null;
  customers: CustomerOption[] = [];
  loading = false;
  errorMessage = '';
  customerSearch = '';
  filters = {
    customerId: null as number | null,
    fromDate: '',
    toDate: ''
  };
  page = 0;
  size = 10;
  readonly pageSizes = [10, 25, 50];
  duesMode = false;

  constructor(
    private customerService: CustomerService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.duesMode = this.route.snapshot.data['mode'] === 'dues';
    const customerId = Number(this.route.snapshot.queryParamMap.get('customerId') || 0);
    if (customerId) {
      this.filters.customerId = customerId;
    }
    this.searchCustomers();
    this.loadReport();
  }

  loadReport(): void {
    this.loading = true;
    this.errorMessage = '';
    this.customerService.getCustomerAging(this.filters.customerId, this.filters.fromDate, this.filters.toDate).subscribe({
      next: report => {
        this.report = report;
        this.page = 0;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Customer aging report could not be loaded.');
        debugApiError('CustomerAgingComponent.loadReport', error);
      }
    });
  }

  searchCustomers(): void {
    this.customerService.searchCustomers(this.customerSearch).subscribe({
      next: customers => this.customers = customers,
      error: error => debugApiError('CustomerAgingComponent.searchCustomers', error)
    });
  }

  resetFilters(): void {
    this.customerSearch = '';
    this.filters = { customerId: null, fromDate: '', toDate: '' };
    this.searchCustomers();
    this.loadReport();
  }

  backToCustomers(): void {
    this.router.navigate(['/customers/list']);
  }

  receivePayment(row: CustomerAgingRow): void {
    if (!this.hasPermission('CUSTOMER_RECEIPT_CREATE')) return;
    this.router.navigate(['/customers/receipts/create'], {
      queryParams: {
        customerId: row.customerId,
        dueAmount: row.totalDue,
        customerName: row.customerName
      }
    });
  }

  viewLedger(row: CustomerAgingRow): void {
    this.router.navigate(['/customers/details', row.customerId], { queryParams: { tab: 'ledger' } });
  }

  overdueAmount(row: CustomerAgingRow): number {
    return Number(row.days1To30 || 0) + Number(row.days31To60 || 0)
      + Number(row.days61To90 || 0) + Number(row.days90Plus || 0);
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  get rows(): CustomerAgingRow[] {
    return this.report?.rows || [];
  }

  get totalPages(): number {
    return Math.max(Math.ceil(this.rows.length / this.size), 1);
  }

  get pagedRows(): CustomerAgingRow[] {
    const start = this.page * this.size;
    return this.rows.slice(start, start + this.size);
  }

  changePageSize(): void {
    this.page = 0;
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) {
      return;
    }
    this.page = page;
  }

  exportCsv(): void {
    const header = ['Customer', 'Current', '1-30 Days', '31-60 Days', '61-90 Days', '90+ Days', 'Total Due'];
    const rows = this.rows.map(row => [
      `${row.customerCode || ''} ${row.customerName || ''}`.trim(),
      this.formatNumber(row.current),
      this.formatNumber(row.days1To30),
      this.formatNumber(row.days31To60),
      this.formatNumber(row.days61To90),
      this.formatNumber(row.days90Plus),
      this.formatNumber(row.totalDue)
    ]);
    const csv = [header, ...rows].map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'customer-aging.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  trackRow(_: number, row: CustomerAgingRow): number {
    return row.customerId;
  }

  private formatNumber(value: number | null | undefined): string {
    return Number(value || 0).toFixed(2);
  }
}
