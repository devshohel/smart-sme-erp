import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PaymentStatus } from '../../../models/sales-common.model';
import { SalesInvoice } from '../../../models/sales-invoice.model';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-sales-invoice-list',
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.css']
})
export class InvoiceListComponent implements OnInit {
  invoices: SalesInvoice[] = [];
  loading = false;
  actionInvoiceId: number | null = null;
  errorMessage = '';
  successMessage = '';
  searchTerm = '';
  statusFilter = '';
  paymentStatusFilter = '';
  dateFrom = '';
  dateTo = '';
  currentPage = 1;
  readonly pageSize = 10;

  readonly statuses: string[] = ['DRAFT', 'POSTED', 'CANCELLED', 'RETURNED'];
  readonly paymentStatuses: PaymentStatus[] = ['PAID', 'PARTIAL', 'DUE'];

  constructor(
    private invoiceService: SalesInvoiceService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  get filteredInvoices(): SalesInvoice[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.invoices.filter(invoice => {
      const invoiceDate = (invoice.saleDate || '').slice(0, 10);
      return (!keyword
          || (invoice.invoiceNo || '').toLowerCase().includes(keyword)
          || (invoice.customerName || '').toLowerCase().includes(keyword))
        && (!this.statusFilter || this.displayStatus(invoice) === this.statusFilter)
        && (!this.paymentStatusFilter || invoice.paymentStatus === this.paymentStatusFilter)
        && (!this.dateFrom || invoiceDate >= this.dateFrom)
        && (!this.dateTo || invoiceDate <= this.dateTo);
    }).sort((a, b) => {
      const timeDifference = new Date(b.createdAt || b.saleDate).getTime() - new Date(a.createdAt || a.saleDate).getTime();
      return timeDifference || Number(b.id || 0) - Number(a.id || 0);
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredInvoices.length / this.pageSize));
  }

  get paginatedInvoices(): SalesInvoice[] {
    const page = Math.min(this.currentPage, this.totalPages);
    const start = (page - 1) * this.pageSize;
    return this.filteredInvoices.slice(start, start + this.pageSize);
  }

  get pageStart(): number { return this.filteredInvoices.length ? (Math.min(this.currentPage, this.totalPages) - 1) * this.pageSize + 1 : 0; }
  get pageEnd(): number { return Math.min(this.pageStart + this.pageSize - 1, this.filteredInvoices.length); }

  get totalSales(): number {
    return this.activeInvoices.length;
  }

  get completedSales(): number {
    return this.invoices.filter(invoice => this.isCompleted(invoice)).length;
  }

  get totalRevenue(): number {
    return this.activeInvoices.reduce((sum, invoice) => sum + Number(invoice.paidAmount || 0), 0);
  }

  get totalDue(): number {
    return this.activeInvoices.reduce((sum, invoice) => sum + Number(invoice.dueAmount || 0), 0);
  }

  loadInvoices(): void {
    this.loading = true;
    this.errorMessage = '';
    this.invoiceService.getAllInvoices().subscribe({
      next: invoices => {
        this.invoices = invoices;
        this.loading = false;
      },
      error: error => {
        this.invoices = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sales could not be loaded.');
        debugApiError('InvoiceListComponent.loadInvoices', error);
      }
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = '';
    this.paymentStatusFilter = '';
    this.dateFrom = '';
    this.dateTo = '';
    this.currentPage = 1;
  }

  filtersChanged(): void { this.currentPage = 1; }

  goToPage(page: number): void {
    this.currentPage = Math.min(Math.max(page, 1), this.totalPages);
  }

  view(invoice: SalesInvoice): void {
    if (invoice.id) this.router.navigate(['/sales', invoice.id, 'view']);
  }

  edit(invoice: SalesInvoice): void {
    if (this.canEdit(invoice)) this.router.navigate(['/sales', invoice.id, 'edit']);
  }

  print(invoice: SalesInvoice): void {
    if (this.canPrint(invoice)) this.router.navigate(['/sales', invoice.id, 'view'], { queryParams: { print: true } });
  }

  post(invoice: SalesInvoice): void {
    if (!this.canPost(invoice) || !invoice.id) return;
    const label = invoice.invoiceNo || `#${invoice.id}`;
    if (!window.confirm(`Post invoice ${label}? Stock and accounting entries will be created.`)) return;

    this.actionInvoiceId = invoice.id;
    this.invoiceService.postInvoice(invoice.id).subscribe({
      next: saved => {
        this.actionInvoiceId = null;
        this.successMessage = `Invoice ${saved.invoiceNo || label} was posted.`;
        this.loadInvoices();
      },
      error: error => {
        this.actionInvoiceId = null;
        this.errorMessage = extractApiErrorMessage(error, 'Invoice posting failed.');
        debugApiError('InvoiceListComponent.post', error);
      }
    });
  }

  deleteDraft(invoice: SalesInvoice): void {
    if (!this.canDelete(invoice) || !invoice.id) return;
    const label = invoice.invoiceNo || `#${invoice.id}`;
    if (!window.confirm(`Delete draft invoice ${label}?`)) return;

    this.actionInvoiceId = invoice.id;
    this.invoiceService.deleteDraftInvoice(invoice.id).subscribe({
      next: () => {
        this.actionInvoiceId = null;
        this.successMessage = `Draft invoice ${label} was deleted.`;
        this.loadInvoices();
      },
      error: error => {
        this.actionInvoiceId = null;
        this.errorMessage = extractApiErrorMessage(error, 'Draft invoice delete failed.');
        debugApiError('InvoiceListComponent.deleteDraft', error);
      }
    });
  }

  receivePayment(invoice: SalesInvoice): void {
    if (this.canReceivePayment(invoice) && invoice.customerId) {
      this.router.navigate(['/customers/receipts/create'], { queryParams: {
        customerId: invoice.customerId,
        invoiceId: invoice.id,
        invoiceNo: invoice.invoiceNo,
        dueAmount: invoice.dueAmount,
        customerName: invoice.customerName
      } });
    }
  }

  createReturn(invoice: SalesInvoice): void {
    if (this.canReturn(invoice)) {
      this.router.navigate(['/sales/returns/add'], { queryParams: { invoiceId: invoice.id } });
    }
  }

  canEdit(invoice: SalesInvoice): boolean {
    return invoice.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_EDIT');
  }

  canPrint(invoice: SalesInvoice): boolean {
    return !!invoice.id && invoice.status !== 'DRAFT' && this.hasPermission('SALES_INVOICE_PRINT');
  }

  canPost(invoice: SalesInvoice): boolean {
    return invoice.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_POST');
  }

  canDelete(invoice: SalesInvoice): boolean {
    return invoice.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_CANCEL');
  }

  canReceivePayment(invoice: SalesInvoice): boolean {
    return invoice.status === 'POSTED' && Number(invoice.dueAmount || 0) > 0
      && this.hasPermission('CUSTOMER_RECEIPT_CREATE');
  }

  canReturn(invoice: SalesInvoice): boolean {
    return invoice.status === 'POSTED' && this.hasPermission('SALES_RETURN_CREATE');
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  displayStatus(invoice: SalesInvoice): string {
    return invoice.status || 'DRAFT';
  }

  private get activeInvoices(): SalesInvoice[] {
    return this.invoices.filter(invoice => !['CANCELLED'].includes(invoice.status || ''));
  }

  private isCompleted(invoice: SalesInvoice): boolean {
    return invoice.status === 'POSTED';
  }
}
