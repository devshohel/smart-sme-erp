import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierAgingReport, SupplierAgingRow, SupplierOption } from '../../../models/supplier.model';
import { SupplierService } from '../../../services/supplier.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-supplier-aging',
  templateUrl: './supplier-aging.component.html',
  styleUrls: ['./supplier-aging.component.css']
})
export class SupplierAgingComponent implements OnInit {
  report: SupplierAgingReport | null = null;
  loading = false;
  errorMessage = '';
  filters = {
    supplierId: null as number | null,
    fromDate: '',
    toDate: ''
  };
  supplierSearchTerm = '';
  supplierSuggestions: SupplierOption[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private supplierService: SupplierService
  ) {}

  ngOnInit(): void {
    const supplierId = Number(this.route.snapshot.queryParamMap.get('supplierId'));
    if (supplierId) {
      this.filters.supplierId = supplierId;
      this.supplierService.getSupplierById(supplierId).subscribe({
        next: supplier => this.supplierSearchTerm = `${supplier.supplierCode || 'SUP'} - ${supplier.name}`,
        error: error => debugApiError('SupplierAgingComponent.loadSupplier', error)
      });
    }
    this.loadReport();
  }

  loadReport(): void {
    this.loading = true;
    this.errorMessage = '';
    this.supplierService.getSupplierAging(this.filters.supplierId, this.filters.fromDate, this.filters.toDate).subscribe({
      next: report => {
        this.report = report;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Supplier aging report could not be loaded.');
        debugApiError('SupplierAgingComponent.loadReport', error);
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
      error: error => debugApiError('SupplierAgingComponent.searchSuppliers', error)
    });
  }

  selectSupplier(supplier: SupplierOption): void {
    this.filters.supplierId = supplier.id;
    this.supplierSearchTerm = `${supplier.supplierCode || 'SUP'} - ${supplier.name}`;
    this.supplierSuggestions = [];
  }

  viewSupplier(row: SupplierAgingRow): void {
    this.router.navigate(['/suppliers/details', row.supplierId]);
  }

  exportCsv(): void {
    const rows = [
      ['Supplier Code', 'Supplier', 'Current', '1-30', '31-60', '61-90', '90+', 'Total Due'],
      ...(this.report?.rows || []).map(row => [
        row.supplierCode || '',
        row.supplierName || '',
        this.formatNumber(row.current),
        this.formatNumber(row.days1To30),
        this.formatNumber(row.days31To60),
        this.formatNumber(row.days61To90),
        this.formatNumber(row.days90Plus),
        this.formatNumber(row.totalDue)
      ])
    ];
    const csv = rows.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'supplier-aging.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  print(): void {
    window.print();
  }

  private formatNumber(value: number | null | undefined): string {
    return Number(value || 0).toFixed(2);
  }
}
