import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerDetail, CustomerLedger, CustomerLedgerEntry, CustomerTransaction } from '../../../models/customer.model';
import { Status } from '../../../models/product.model';
import { CustomerService } from '../../../services/customer.service';
import { AuthService } from '../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-customer-details',
  templateUrl: './customer-details.component.html',
  styleUrls: ['./customer-details.component.css']
})
export class CustomerDetailsComponent implements OnInit {
  detail: CustomerDetail | null = null;
  ledger: CustomerLedger | null = null;
  loading = false;
  ledgerLoading = false;
  errorMessage = '';
  ledgerError = '';
  activeTab: 'overview' | 'invoices' | 'receipts' | 'ledger' | 'statement' = 'overview';
  ledgerFilters = { fromDate: '', toDate: '' };
  ledgerPage = 0;
  ledgerSize = 10;
  readonly ledgerPageSizes = [10, 25, 50];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private customerService: CustomerService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const requestedTab = this.route.snapshot.queryParamMap.get('tab');
    if (requestedTab && ['overview', 'invoices', 'receipts', 'ledger', 'statement'].includes(requestedTab)) {
      this.activeTab = requestedTab as 'overview' | 'invoices' | 'receipts' | 'ledger' | 'statement';
    }
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Customer id is missing.';
      return;
    }
    this.loadCustomerDetail(id);
  }

  loadCustomerDetail(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.customerService.getCustomerDetail(id).subscribe({
      next: detail => {
        this.detail = detail;
        this.loading = false;
        this.loadLedger();
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Customer details could not be loaded.');
        debugApiError('CustomerDetailsComponent.loadCustomerDetail', error);
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/customers/list']);
  }

  editCustomer(): void {
    if (this.detail?.customer.id) {
      this.router.navigate(['/customers/edit', this.detail.customer.id]);
    }
  }

  newReceipt(): void {
    if (this.detail?.customer.id) {
      this.router.navigate(['/customers/receipts/create'], {
        queryParams: {
          customerId: this.detail.customer.id,
          customerName: this.detail.customer.name,
          dueAmount: this.detail.totalDue
        }
      });
    }
  }

  receiveInvoicePayment(invoice: CustomerTransaction): void {
    if (!this.detail?.customer.id || invoice.due <= 0 || !this.hasPermission('CUSTOMER_RECEIPT_CREATE')) return;
    this.router.navigate(['/customers/receipts/create'], { queryParams: {
      customerId: this.detail.customer.id,
      customerName: this.detail.customer.name,
      invoiceId: invoice.id,
      invoiceNo: invoice.documentNo,
      dueAmount: invoice.due
    } });
  }

  openAgingReport(): void {
    this.router.navigate(['/customers/aging'], {
      queryParams: this.detail?.customer.id ? { customerId: this.detail.customer.id } : {}
    });
  }

  statusClass(status?: Status): string {
    if (status === 'ACTIVE') {
      return 'active';
    }
    if (status === 'INACTIVE') {
      return 'inactive';
    }
    return 'neutral';
  }

  balanceStatusClass(status?: string): string {
    if (status === 'Over Limit') {
      return 'over';
    }
    if (status === 'Near Credit Limit') {
      return 'near';
    }
    return 'normal';
  }

  setActiveTab(tab: 'overview' | 'invoices' | 'receipts' | 'ledger' | 'statement'): void {
    this.activeTab = tab;
  }

  loadLedger(): void {
    const customerId = this.detail?.customer.id;
    if (!customerId) {
      return;
    }

    this.ledgerLoading = true;
    this.ledgerError = '';
    this.customerService.getCustomerLedger(customerId, this.ledgerFilters.fromDate, this.ledgerFilters.toDate).subscribe({
      next: ledger => {
        this.ledger = ledger;
        this.ledgerPage = 0;
        this.ledgerLoading = false;
      },
      error: error => {
        this.ledgerLoading = false;
        this.ledgerError = extractApiErrorMessage(error, 'Customer ledger could not be loaded.');
        debugApiError('CustomerDetailsComponent.loadLedger', error);
      }
    });
  }

  resetLedgerFilters(): void {
    this.ledgerFilters = { fromDate: '', toDate: '' };
    this.loadLedger();
  }

  get ledgerEntries(): CustomerLedgerEntry[] {
    return this.ledger?.entries || [];
  }

  get ledgerTotalPages(): number {
    return Math.max(Math.ceil(this.ledgerEntries.length / this.ledgerSize), 1);
  }

  get pagedLedgerEntries(): CustomerLedgerEntry[] {
    const start = this.ledgerPage * this.ledgerSize;
    return this.ledgerEntries.slice(start, start + this.ledgerSize);
  }

  changeLedgerPageSize(): void {
    this.ledgerPage = 0;
  }

  goToLedgerPage(page: number): void {
    if (page < 0 || page >= this.ledgerTotalPages || page === this.ledgerPage) {
      return;
    }
    this.ledgerPage = page;
  }

  exportLedgerCsv(): void {
    const header = ['Date', 'Reference Type', 'Reference No', 'Description', 'Debit', 'Credit', 'Running Balance'];
    const rows = this.ledgerEntries.map(entry => [
      entry.date || '',
      entry.referenceType || '',
      entry.referenceNo || '',
      entry.description || '',
      this.formatNumber(entry.debit),
      this.formatNumber(entry.credit),
      this.formatNumber(entry.runningBalance)
    ]);
    this.downloadCsv('customer-ledger.csv', [header, ...rows]);
  }

  printStatement(): void {
    window.print();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  trackTransaction(_: number, transaction: CustomerTransaction): number {
    return transaction.id;
  }

  trackReceipt(_: number, receipt: { id?: number | null }): number | null | undefined {
    return receipt.id;
  }

  trackLedger(index: number): number {
    return index;
  }

  private downloadCsv(filename: string, rows: string[][]): void {
    const csv = rows.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  private formatNumber(value: number | null | undefined): string {
    return Number(value || 0).toFixed(2);
  }
}
