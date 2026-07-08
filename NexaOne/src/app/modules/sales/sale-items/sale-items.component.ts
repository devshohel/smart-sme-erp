import { Component, OnInit } from '@angular/core';
import { SalesInvoiceStatus } from '../../../models/sales-common.model';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

interface SaleItemRow {
  invoiceId?: number;
  invoiceNo: string;
  customerName: string;
  saleDate: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  tax: number;
  subtotal: number;
  status?: SalesInvoiceStatus;
}

@Component({
  selector: 'app-sale-items',
  templateUrl: './sale-items.component.html',
  styleUrls: ['./sale-items.component.css']
})
export class SaleItemsComponent implements OnInit {
  rows: SaleItemRow[] = [];
  loading = false;
  errorMessage = '';
  searchTerm = '';
  statusFilter = '';
  dateFrom = '';
  dateTo = '';
  currentPage = 1;
  readonly pageSize = 15;
  readonly statuses: string[] = ['DRAFT', 'POSTED', 'CANCELLED', 'RETURNED'];

  constructor(private invoiceService: SalesInvoiceService) {}

  ngOnInit(): void {
    this.loading = true;
    this.invoiceService.getAllInvoices().subscribe({
      next: invoices => {
        this.rows = invoices.flatMap(invoice => (invoice.items || []).map(item => ({
          invoiceId: invoice.id,
          invoiceNo: invoice.invoiceNo || `Draft #${invoice.id}`,
          customerName: invoice.customerName || 'N/A',
          saleDate: invoice.saleDate,
          productName: item.productName || 'N/A',
          quantity: Number(item.quantity || 0),
          unitPrice: Number(item.unitPrice || 0),
          discount: Number(item.discount || 0),
          tax: Number(item.tax || 0),
          subtotal: Number(item.subtotal || 0),
          status: invoice.status
        })));
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sale items could not be loaded.');
        debugApiError('SaleItemsComponent.load', error);
      }
    });
  }

  get filteredRows(): SaleItemRow[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.rows.filter(row => {
      const saleDate = (row.saleDate || '').slice(0, 10);
      const matchesKeyword = !keyword || row.invoiceNo.toLowerCase().includes(keyword)
        || row.customerName.toLowerCase().includes(keyword) || row.productName.toLowerCase().includes(keyword);
      return matchesKeyword && (!this.statusFilter || this.statusLabel(row) === this.statusFilter)
        && (!this.dateFrom || saleDate >= this.dateFrom) && (!this.dateTo || saleDate <= this.dateTo);
    });
  }

  get totalPages(): number { return Math.max(1, Math.ceil(this.filteredRows.length / this.pageSize)); }
  get paginatedRows(): SaleItemRow[] {
    const page = Math.min(this.currentPage, this.totalPages);
    return this.filteredRows.slice((page - 1) * this.pageSize, page * this.pageSize);
  }
  get pageStart(): number { return this.filteredRows.length ? (Math.min(this.currentPage, this.totalPages) - 1) * this.pageSize + 1 : 0; }
  get pageEnd(): number { return Math.min(this.pageStart + this.pageSize - 1, this.filteredRows.length); }
  filtersChanged(): void { this.currentPage = 1; }
  goToPage(page: number): void { this.currentPage = Math.min(Math.max(page, 1), this.totalPages); }
  statusLabel(row: SaleItemRow): string {
    if (row.status === 'CANCELLED' || row.status === 'REVERSED') return 'CANCELLED';
    if (row.status === 'RETURNED') return 'RETURNED';
    if (['SUBMITTED', 'APPROVED', 'PENDING', 'CONFIRMED', 'COMPLETED', 'PAID', 'PARTIAL_PAID'].includes(row.status || '')) return 'POSTED';
    if (row.status === 'POSTED') return 'POSTED';
    return 'DRAFT';
  }
}
