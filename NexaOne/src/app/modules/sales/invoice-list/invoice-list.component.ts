import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PaymentStatus, SalesInvoiceStatus } from '../../../models/sales-common.model';
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

  readonly statuses: SalesInvoiceStatus[] = ['DRAFT', 'SUBMITTED', 'APPROVED', 'POSTED', 'PARTIAL_PAID', 'PAID', 'CANCELLED'];
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
        && (!this.statusFilter || invoice.status === this.statusFilter)
        && (!this.paymentStatusFilter || invoice.paymentStatus === this.paymentStatusFilter)
        && (!this.dateFrom || invoiceDate >= this.dateFrom)
        && (!this.dateTo || invoiceDate <= this.dateTo);
    }).sort((a, b) => {
      const timeDifference = new Date(b.createdAt || b.saleDate).getTime() - new Date(a.createdAt || a.saleDate).getTime();
      return timeDifference || Number(b.id || 0) - Number(a.id || 0);
    });
  }

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

  cancel(invoice: SalesInvoice): void {
    if (!this.canCancel(invoice) || !invoice.id) return;
    const label = invoice.invoiceNo || `#${invoice.id}`;
    if (!window.confirm(`Cancel invoice ${label}? This action cannot be undone.`)) return;

    this.actionInvoiceId = invoice.id;
    this.invoiceService.cancelInvoice(invoice.id).subscribe({
      next: saved => {
        this.actionInvoiceId = null;
        this.successMessage = `Invoice ${saved.invoiceNo || label} was cancelled.`;
        this.loadInvoices();
      },
      error: error => {
        this.actionInvoiceId = null;
        this.errorMessage = extractApiErrorMessage(error, 'Invoice cancellation failed.');
        debugApiError('InvoiceListComponent.cancel', error);
      }
    });
  }

  receivePayment(invoice: SalesInvoice): void {
    if (this.canReceivePayment(invoice) && invoice.customerId) {
      this.router.navigate(['/customers/receipts/create'], { queryParams: { customerId: invoice.customerId } });
    }
  }

  createReturn(invoice: SalesInvoice): void {
    if (this.canReturn(invoice)) {
      this.router.navigate(['/sales/returns/create'], { queryParams: { invoiceId: invoice.id } });
    }
  }

  canEdit(invoice: SalesInvoice): boolean {
    return invoice.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_EDIT');
  }

  canPrint(invoice: SalesInvoice): boolean {
    return !!invoice.id && this.hasPermission('SALES_INVOICE_PRINT');
  }

  canCancel(invoice: SalesInvoice): boolean {
    return ['DRAFT', 'SUBMITTED', 'APPROVED'].includes(invoice.status || '')
      && this.hasPermission('SALES_INVOICE_CANCEL');
  }

  canReceivePayment(invoice: SalesInvoice): boolean {
    return Number(invoice.dueAmount || 0) > 0 && this.isCompleted(invoice)
      && this.hasPermission('CUSTOMER_RECEIPT_CREATE');
  }

  canReturn(invoice: SalesInvoice): boolean {
    return this.isCompleted(invoice) && this.hasPermission('SALES_RETURN_CREATE');
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  private get activeInvoices(): SalesInvoice[] {
    return this.invoices.filter(invoice => !['CANCELLED', 'REVERSED'].includes(invoice.status || ''));
  }

  private isCompleted(invoice: SalesInvoice): boolean {
    return ['POSTED', 'PARTIAL_PAID', 'PAID', 'COMPLETED'].includes(invoice.status || '');
  }
}
