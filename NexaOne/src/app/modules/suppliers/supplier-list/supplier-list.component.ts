import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Status } from '../../../models/product.model';
import { Supplier } from '../../../models/supplier.model';
import { SupplierService } from '../../../services/supplier.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-supplier-list',
  templateUrl: './supplier-list.component.html',
  styleUrls: ['./supplier-list.component.css']
})
export class SupplierListComponent implements OnInit {
  suppliers: Supplier[] = [];
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    status: '' as Status | ''
  };

  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];
  readonly pageSizes = [10, 25, 50, 100];
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  sort = 'createdAt';
  direction: 'asc' | 'desc' = 'desc';

  constructor(
    private supplierService: SupplierService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.supplierService.getSupplierPage({
      keyword: this.filters.keyword,
      status: this.filters.status,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    }).subscribe({
      next: (data) => {
        this.suppliers = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.page = data.page;
        this.size = data.size;
        this.loading = false;
      },
      error: (error) => {
        this.suppliers = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Suppliers could not be loaded.');
        debugApiError('SupplierListComponent.loadSuppliers', error);
      }
    });
  }

  search(): void {
    this.page = 0;
    this.loadSuppliers();
  }

  resetFilters(): void {
    this.filters = {
      keyword: '',
      status: ''
    };
    this.page = 0;
    this.sort = 'createdAt';
    this.direction = 'desc';
    this.loadSuppliers();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadSuppliers();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) {
      return;
    }
    this.page = page;
    this.loadSuppliers();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = column === 'name' || column === 'supplierCode' ? 'asc' : 'desc';
    }
    this.loadSuppliers();
  }

  sortIcon(column: string): string {
    if (this.sort !== column) {
      return 'bi-arrow-down-up';
    }
    return this.direction === 'asc' ? 'bi-sort-alpha-down' : 'bi-sort-alpha-up';
  }

  createSupplier(): void {
    this.router.navigate(['/suppliers/create']);
  }

  editSupplier(supplier: Supplier): void {
    if (supplier.id) {
      this.router.navigate(['/suppliers/edit', supplier.id]);
    }
  }

  viewSupplier(supplier: Supplier): void {
    if (supplier.id) {
      this.router.navigate(['/suppliers/details', supplier.id]);
    }
  }

  newPayment(supplier: Supplier): void {
    if (supplier.id) {
      this.router.navigate(['/suppliers/payments/create'], {
        queryParams: { supplierId: supplier.id }
      });
    }
  }

  viewLedger(supplier: Supplier): void {
    if (supplier.id) {
      this.router.navigate(['/suppliers/details', supplier.id], {
        queryParams: { tab: 'ledger' }
      });
    }
  }

  viewAging(supplier: Supplier): void {
    this.router.navigate(['/suppliers/aging'], {
      queryParams: supplier.id ? { supplierId: supplier.id } : undefined
    });
  }

  viewApReconciliation(): void {
    this.router.navigate(['/suppliers/ap-reconciliation']);
  }

  deleteSupplier(supplier: Supplier): void {
    if (!supplier.id) {
      return;
    }

    if (confirm(`Are you sure you want to delete supplier "${supplier.name}"?`)) {
      this.supplierService.deleteSupplier(supplier.id).subscribe({
        next: () => this.loadSuppliers(),
        error: (error) => {
          this.errorMessage = extractApiErrorMessage(error, 'Delete request failed.');
          debugApiError('SupplierListComponent.deleteSupplier', error);
        }
      });
    }
  }

  statusClass(status?: Status): string {
    if (status === 'ACTIVE') {
      return 'bg-success-subtle text-success';
    }
    if (status === 'INACTIVE') {
      return 'bg-secondary-subtle text-secondary';
    }
    return 'bg-light text-dark';
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  exportCsv(): void {
    const rows = [this.exportHeaders(), ...this.suppliers.map(supplier => this.exportValues(supplier))];
    const csv = rows.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    this.downloadBlob(new Blob([csv], { type: 'text/csv;charset=utf-8;' }), 'suppliers.csv');
  }

  exportExcel(): void {
    const rows = [this.exportHeaders(), ...this.suppliers.map(supplier => this.exportValues(supplier))];
    const html = `<table>${rows.map(row => `<tr>${row.map(cell => `<td>${this.escapeHtml(cell)}</td>`).join('')}</tr>`).join('')}</table>`;
    this.downloadBlob(new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8;' }), 'suppliers.xls');
  }

  private exportHeaders(): string[] {
    return ['Supplier Code', 'Name', 'Company', 'Phone', 'Email', 'City', 'Country', 'Payment Terms', 'Supplier Due', 'Current Balance', 'Status'];
  }

  private exportValues(supplier: Supplier): string[] {
    return [
      supplier.supplierCode || '',
      supplier.name || '',
      supplier.companyName || '',
      supplier.phone || '',
      supplier.email || '',
      supplier.city || '',
      supplier.country || '',
      supplier.paymentTerms || '',
      this.formatNumber(supplier.supplierDue),
      this.formatNumber(supplier.currentBalance),
      supplier.status || ''
    ];
  }

  private formatNumber(value: number | null | undefined): string {
    return Number(value || 0).toFixed(2);
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  private escapeHtml(value: string): string {
    return String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char] || char));
  }
}
