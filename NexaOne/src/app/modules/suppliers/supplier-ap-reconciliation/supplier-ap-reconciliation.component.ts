import { Component, OnInit } from '@angular/core';
import { ApReconciliationReport, ApReconciliationRow, ApReconciliationStatus, SupplierOption } from '../../../models/supplier.model';
import { SupplierService } from '../../../services/supplier.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-supplier-ap-reconciliation',
  templateUrl: './supplier-ap-reconciliation.component.html',
  styleUrls: ['./supplier-ap-reconciliation.component.css']
})
export class SupplierApReconciliationComponent implements OnInit {
  report: ApReconciliationReport | null = null;
  loading = false;
  errorMessage = '';
  filters = {
    supplierId: null as number | null,
    fromDate: '',
    toDate: ''
  };
  supplierSearchTerm = '';
  supplierSuggestions: SupplierOption[] = [];

  constructor(private supplierService: SupplierService) {}

  ngOnInit(): void {
    this.loadReport();
  }

  loadReport(): void {
    this.loading = true;
    this.errorMessage = '';
    this.supplierService.getApReconciliation(this.filters.supplierId, this.filters.fromDate, this.filters.toDate).subscribe({
      next: report => {
        this.report = report;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'AP reconciliation report could not be loaded.');
        debugApiError('SupplierApReconciliationComponent.loadReport', error);
      }
    });
  }

  resetFilters(): void {
    this.filters = { supplierId: null, fromDate: '', toDate: '' };
    this.supplierSearchTerm = '';
    this.supplierSuggestions = [];
    this.loadReport();
  }

  searchSuppliers(term: string): void {
    this.supplierSearchTerm = term;
    this.filters.supplierId = null;
    if (!term.trim()) {
      this.supplierSuggestions = [];
      return;
    }
    this.supplierService.getSupplierOptions(term).subscribe({
      next: suppliers => this.supplierSuggestions = suppliers,
      error: error => debugApiError('SupplierApReconciliationComponent.searchSuppliers', error)
    });
  }

  selectSupplier(supplier: SupplierOption): void {
    this.filters.supplierId = supplier.id;
    this.supplierSearchTerm = `${supplier.supplierCode || 'SUP'} - ${supplier.name}`;
    this.supplierSuggestions = [];
  }

  statusClass(status: ApReconciliationStatus): string {
    if (status === 'MATCHED') {
      return 'bg-light-success text-success';
    }
    if (status === 'REVIEW_NEEDED') {
      return 'bg-light-warning text-warning';
    }
    return 'bg-light-danger text-danger';
  }

  exportCsv(): void {
    const rows = [
      ['Supplier Code', 'Supplier', 'Purchase Due', 'Supplier Advance', 'GL AP', 'Variance', 'Net Exposure', 'Status'],
      ...(this.report?.rows || []).map(row => [
        row.supplierCode || '',
        row.supplierName || '',
        this.formatNumber(row.purchaseDue),
        this.formatNumber(row.supplierAdvance),
        this.formatNumber(row.glAccountsPayable),
        this.formatNumber(row.variance),
        this.formatNumber(row.netExposure),
        row.status || ''
      ])
    ];
    const csv = rows.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'ap-reconciliation.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  trackRow(index: number, row: ApReconciliationRow): string {
    return row.supplierCode || String(index);
  }

  private formatNumber(value: number | null | undefined): string {
    return Number(value || 0).toFixed(2);
  }
}
