import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierDetail, SupplierLedger, SupplierLedgerEntry, SupplierStatement, SupplierStatementRow } from '../../../models/supplier.model';
import { SupplierService } from '../../../services/supplier.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-supplier-details',
  templateUrl: './supplier-details.component.html',
  styleUrls: ['./supplier-details.component.css']
})
export class SupplierDetailsComponent implements OnInit {
  detail: SupplierDetail | null = null;
  ledger: SupplierLedger | null = null;
  statement: SupplierStatement | null = null;
  loading = false;
  ledgerLoading = false;
  statementLoading = false;
  errorMessage = '';
  ledgerError = '';
  statementError = '';
  activeTab: 'overview' | 'purchases' | 'payments' | 'ledger' | 'statement' = 'overview';
  ledgerFilters = { fromDate: '', toDate: '' };
  statementFilters = { fromDate: '', toDate: '' };
  ledgerPage = 0;
  ledgerSize = 10;
  readonly ledgerPageSizes = [10, 25, 50];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private supplierService: SupplierService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Supplier id is missing.';
      return;
    }
    const tab = this.route.snapshot.queryParamMap.get('tab');
    if (tab === 'ledger') {
      this.activeTab = 'ledger';
    } else if (tab === 'statement') {
      this.activeTab = 'statement';
    }
    this.loadSupplier(id);
  }

  loadSupplier(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.supplierService.getSupplierDetail(id).subscribe({
      next: detail => {
        this.detail = detail;
        this.loading = false;
        this.loadLedger();
        if (this.activeTab === 'statement') {
          this.loadStatement();
        }
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Supplier details could not be loaded.');
        debugApiError('SupplierDetailsComponent.loadSupplier', error);
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/suppliers/list']);
  }

  editSupplier(): void {
    if (this.detail?.supplier.id) {
      this.router.navigate(['/suppliers/edit', this.detail.supplier.id]);
    }
  }

  newPayment(): void {
    if (this.detail?.supplier.id) {
      this.router.navigate(['/suppliers/payments/create'], {
        queryParams: { supplierId: this.detail.supplier.id }
      });
    }
  }

  openAgingReport(): void {
    this.router.navigate(['/suppliers/aging'], {
      queryParams: this.detail?.supplier.id ? { supplierId: this.detail.supplier.id } : {}
    });
  }

  setActiveTab(tab: 'overview' | 'purchases' | 'payments' | 'ledger' | 'statement'): void {
    this.activeTab = tab;
    if (tab === 'statement' && !this.statement) {
      this.loadStatement();
    }
  }

  loadLedger(): void {
    const supplierId = this.detail?.supplier.id;
    if (!supplierId) {
      return;
    }
    this.ledgerLoading = true;
    this.ledgerError = '';
    this.supplierService.getSupplierLedger(supplierId, this.ledgerFilters.fromDate, this.ledgerFilters.toDate).subscribe({
      next: ledger => {
        this.ledger = ledger;
        this.ledgerPage = 0;
        this.ledgerLoading = false;
      },
      error: error => {
        this.ledgerLoading = false;
        this.ledgerError = extractApiErrorMessage(error, 'Supplier ledger could not be loaded.');
        debugApiError('SupplierDetailsComponent.loadLedger', error);
      }
    });
  }

  resetLedgerFilters(): void {
    this.ledgerFilters = { fromDate: '', toDate: '' };
    this.loadLedger();
  }

  loadStatement(): void {
    const supplierId = this.detail?.supplier.id;
    if (!supplierId) {
      return;
    }
    this.statementLoading = true;
    this.statementError = '';
    this.supplierService.getSupplierStatement(supplierId, this.statementFilters.fromDate, this.statementFilters.toDate).subscribe({
      next: statement => {
        this.statement = statement;
        this.statementLoading = false;
      },
      error: error => {
        this.statementLoading = false;
        this.statementError = extractApiErrorMessage(error, 'Supplier statement could not be loaded.');
        debugApiError('SupplierDetailsComponent.loadStatement', error);
      }
    });
  }

  resetStatementFilters(): void {
    this.statementFilters = { fromDate: '', toDate: '' };
    this.loadStatement();
  }

  get ledgerEntries(): SupplierLedgerEntry[] {
    return this.ledger?.entries || [];
  }

  get ledgerTotalPages(): number {
    return Math.max(Math.ceil(this.ledgerEntries.length / this.ledgerSize), 1);
  }

  get pagedLedgerEntries(): SupplierLedgerEntry[] {
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
    this.downloadCsv('supplier-ledger.csv', [header, ...rows]);
  }

  exportStatementCsv(): void {
    const supplierCode = this.statement?.supplier.supplierCode || 'supplier';
    const header = ['Date', 'Reference Type', 'Reference No', 'Description', 'Debit', 'Credit', 'Running Balance', 'Advance Amount', 'Status'];
    const rows = this.statementRows.map(row => [
      row.date || '',
      row.referenceType || '',
      row.referenceNo || '',
      row.description || '',
      this.formatNumber(row.debit),
      this.formatNumber(row.credit),
      this.formatNumber(row.runningBalance),
      this.formatNumber(row.advanceAmount),
      row.status || ''
    ]);
    this.downloadCsv(`${supplierCode}-statement.csv`, [header, ...rows]);
  }

  printStatement(): void {
    window.print();
  }

  get statementRows(): SupplierStatementRow[] {
    return this.statement?.rows || [];
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  trackLedger(index: number): number {
    return index;
  }

  trackStatement(index: number): number {
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
