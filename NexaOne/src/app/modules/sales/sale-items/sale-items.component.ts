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
    if (!keyword) return this.rows;
    return this.rows.filter(row => row.invoiceNo.toLowerCase().includes(keyword)
      || row.customerName.toLowerCase().includes(keyword)
      || row.productName.toLowerCase().includes(keyword));
  }
}
