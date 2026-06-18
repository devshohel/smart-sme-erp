import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SupplierOption } from '../../../../models/supplier.model';
import { SupplierPayment, SupplierPaymentMethod, SupplierPaymentSearchParams, SupplierPaymentStatus } from '../../../../models/supplier-payment.model';
import { SupplierService } from '../../../../services/supplier.service';
import { SupplierPaymentService } from '../../../../services/supplier-payment.service';
import { debugApiError, extractApiErrorMessage } from '../../../../shared/utils/api-error.util';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-supplier-payment-list',
  templateUrl: './supplier-payment-list.component.html',
  styleUrls: ['./supplier-payment-list.component.css']
})
export class SupplierPaymentListComponent implements OnInit {
  payments: SupplierPayment[] = [];
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    supplierId: '' as number | '',
    status: '' as SupplierPaymentStatus | '',
    paymentMethod: '' as SupplierPaymentMethod | '',
    fromDate: '',
    toDate: ''
  };

  supplierSearchTerm = '';
  supplierSuggestions: SupplierOption[] = [];
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  sort = 'paymentDate';
  direction: 'asc' | 'desc' = 'desc';

  readonly pageSizes = [10, 25, 50, 100];
  readonly statusList: SupplierPaymentStatus[] = ['DRAFT', 'POSTED', 'CANCELLED', 'REVERSED'];
  readonly paymentMethods: SupplierPaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'CHEQUE', 'OTHER'];

  constructor(
    private paymentService: SupplierPaymentService,
    private supplierService: SupplierService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(): void {
    this.loading = true;
    this.errorMessage = '';
    const params: SupplierPaymentSearchParams = {
      ...this.filters,
      fromDate: this.filters.fromDate || undefined,
      toDate: this.filters.toDate || undefined,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    };
    this.paymentService.getPaymentPage(params).subscribe({
      next: data => {
        this.payments = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.page = data.page;
        this.size = data.size;
        this.loading = false;
      },
      error: error => {
        this.payments = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payments could not be loaded.');
        debugApiError('SupplierPaymentListComponent.loadPayments', error);
      }
    });
  }

  search(): void {
    this.page = 0;
    this.loadPayments();
  }

  resetFilters(): void {
    this.filters = { keyword: '', supplierId: '', status: '', paymentMethod: '', fromDate: '', toDate: '' };
    this.supplierSearchTerm = '';
    this.supplierSuggestions = [];
    this.page = 0;
    this.sort = 'paymentDate';
    this.direction = 'desc';
    this.loadPayments();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadPayments();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) {
      return;
    }
    this.page = page;
    this.loadPayments();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = 'asc';
    }
    this.loadPayments();
  }

  sortIcon(column: string): string {
    if (this.sort !== column) {
      return 'bi-arrow-down-up';
    }
    return this.direction === 'asc' ? 'bi-sort-alpha-down' : 'bi-sort-alpha-up';
  }

  createPayment(): void {
    this.router.navigate(['/suppliers/payments/create']);
  }

  viewPayment(payment: SupplierPayment): void {
    if (payment.id) {
      this.router.navigate(['/suppliers/payments/details', payment.id]);
    }
  }

  editPayment(payment: SupplierPayment): void {
    if (payment.id && payment.status === 'DRAFT') {
      this.router.navigate(['/suppliers/payments/edit', payment.id]);
    }
  }

  postPayment(payment: SupplierPayment): void {
    if (!payment.id || payment.status !== 'DRAFT') {
      return;
    }
    this.paymentService.postPayment(payment.id).subscribe({
      next: () => this.loadPayments(),
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be posted.');
        debugApiError('SupplierPaymentListComponent.postPayment', error);
      }
    });
  }

  cancelPayment(payment: SupplierPayment): void {
    if (!payment.id || payment.status !== 'DRAFT') {
      return;
    }
    if (!confirm(`Cancel supplier payment "${payment.paymentNo || payment.id}"?`)) {
      return;
    }
    this.paymentService.cancelPayment(payment.id).subscribe({
      next: () => this.loadPayments(),
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be cancelled.');
        debugApiError('SupplierPaymentListComponent.cancelPayment', error);
      }
    });
  }

  reversePayment(payment: SupplierPayment): void {
    if (!payment.id || payment.status !== 'POSTED' || !payment.canReverse) {
      return;
    }
    const reason = prompt(`Reverse supplier payment "${payment.paymentNo || payment.id}"? Enter reversal reason:`);
    if (reason === null) {
      return;
    }
    this.paymentService.reversePayment(payment.id, reason).subscribe({
      next: () => this.loadPayments(),
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be reversed.');
        debugApiError('SupplierPaymentListComponent.reversePayment', error);
      }
    });
  }

  onSupplierSearch(term: string): void {
    this.supplierSearchTerm = term;
    this.filters.supplierId = '';
    if (!term.trim()) {
      this.supplierSuggestions = [];
      return;
    }
    this.supplierService.getSupplierOptions(term).subscribe({
      next: suppliers => this.supplierSuggestions = suppliers,
      error: error => debugApiError('SupplierPaymentListComponent.onSupplierSearch', error)
    });
  }

  selectSupplier(supplier: SupplierOption): void {
    this.filters.supplierId = supplier.id;
    this.supplierSearchTerm = `${supplier.supplierCode || 'SUP'} - ${supplier.name}`;
    this.supplierSuggestions = [];
  }

  openCreateForSupplier(): void {
    this.router.navigate(['/suppliers/payments/create'], {
      queryParams: this.filters.supplierId ? { supplierId: this.filters.supplierId } : undefined
    });
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  paymentMethodClass(method?: SupplierPaymentMethod): string {
    if (method === 'BANK' || method === 'CHEQUE') return 'bg-light-primary text-primary';
    if (method === 'MOBILE_BANKING') return 'bg-light-info text-info';
    if (method === 'OTHER') return 'bg-light-secondary text-secondary';
    return 'bg-light-success text-success';
  }

  statusClass(status?: SupplierPaymentStatus): string {
    if (status === 'POSTED') return 'bg-light-success text-success';
    if (status === 'CANCELLED') return 'bg-light-danger text-danger';
    if (status === 'REVERSED') return 'bg-light-secondary text-secondary';
    return 'bg-light-warning text-warning';
  }
}
