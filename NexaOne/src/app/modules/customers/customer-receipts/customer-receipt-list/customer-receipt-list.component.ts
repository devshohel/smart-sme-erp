import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CustomerOption } from '../../../../models/customer.model';
import { CustomerReceipt, CustomerReceiptPaymentMethod, CustomerReceiptSearchParams, CustomerReceiptStatus } from '../../../../models/customer-receipt.model';
import { CustomerService } from '../../../../services/customer.service';
import { CustomerReceiptService } from '../../../../services/customer-receipt.service';
import { AuthService } from '../../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../../shared/utils/api-error.util';

type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-customer-receipt-list',
  templateUrl: './customer-receipt-list.component.html',
  styleUrls: ['./customer-receipt-list.component.css']
})
export class CustomerReceiptListComponent implements OnInit {
  receipts: CustomerReceipt[] = [];
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    customerId: '' as number | '',
    status: '' as CustomerReceiptStatus | '',
    paymentMethod: '' as CustomerReceiptPaymentMethod | '',
    fromDate: '',
    toDate: ''
  };

  customerSearchTerm = '';
  customerSuggestions: CustomerOption[] = [];

  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  sort = 'receiptDate';
  direction: SortDirection = 'desc';

  readonly pageSizes = [10, 25, 50, 100];
  readonly statusList: CustomerReceiptStatus[] = ['DRAFT', 'POSTED', 'CANCELLED'];
  readonly paymentMethods: CustomerReceiptPaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'CHEQUE', 'OTHER'];

  constructor(
    private receiptService: CustomerReceiptService,
    private customerService: CustomerService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReceipts();
  }

  loadReceipts(): void {
    this.loading = true;
    this.errorMessage = '';

    const params: CustomerReceiptSearchParams = {
      keyword: this.filters.keyword,
      customerId: this.filters.customerId,
      status: this.filters.status,
      paymentMethod: this.filters.paymentMethod,
      fromDate: this.filters.fromDate || undefined,
      toDate: this.filters.toDate || undefined,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    };

    this.receiptService.getReceiptPage(params).subscribe({
      next: data => {
        this.receipts = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.page = data.page;
        this.size = data.size;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.receipts = [];
        this.errorMessage = extractApiErrorMessage(error, 'Customer receipts could not be loaded.');
        debugApiError('CustomerReceiptListComponent.loadReceipts', error);
      }
    });
  }

  search(): void {
    this.page = 0;
    this.loadReceipts();
  }

  resetFilters(): void {
    this.filters = {
      keyword: '',
      customerId: '',
      status: '',
      paymentMethod: '',
      fromDate: '',
      toDate: ''
    };
    this.customerSearchTerm = '';
    this.customerSuggestions = [];
    this.page = 0;
    this.sort = 'receiptDate';
    this.direction = 'desc';
    this.loadReceipts();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadReceipts();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) {
      return;
    }
    this.page = page;
    this.loadReceipts();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = 'asc';
    }
    this.loadReceipts();
  }

  sortIcon(column: string): string {
    if (this.sort !== column) {
      return 'bi-arrow-down-up';
    }
    return this.direction === 'asc' ? 'bi-sort-alpha-down' : 'bi-sort-alpha-up';
  }

  createReceipt(): void {
    this.router.navigate(['/customers/receipts/create']);
  }

  viewReceipt(receipt: CustomerReceipt): void {
    if (receipt.id) {
      this.router.navigate(['/customers/receipts/details', receipt.id]);
    }
  }

  editReceipt(receipt: CustomerReceipt): void {
    if (receipt.id && receipt.status === 'DRAFT') {
      this.router.navigate(['/customers/receipts/edit', receipt.id]);
    }
  }

  postReceipt(receipt: CustomerReceipt): void {
    if (!receipt.id || receipt.status !== 'DRAFT') {
      return;
    }
    this.receiptService.postReceipt(receipt.id).subscribe({
      next: () => this.loadReceipts(),
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Receipt could not be posted.');
        debugApiError('CustomerReceiptListComponent.postReceipt', error);
      }
    });
  }

  cancelReceipt(receipt: CustomerReceipt): void {
    if (!receipt.id || receipt.status !== 'DRAFT') {
      return;
    }
    if (!confirm(`Cancel receipt "${receipt.receiptNo || receipt.id}"?`)) {
      return;
    }
    this.receiptService.cancelReceipt(receipt.id).subscribe({
      next: () => this.loadReceipts(),
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Receipt could not be cancelled.');
        debugApiError('CustomerReceiptListComponent.cancelReceipt', error);
      }
    });
  }

  onCustomerSearch(term: string): void {
    this.customerSearchTerm = term;
    this.filters.customerId = '';
    if (!term.trim()) {
      this.customerSuggestions = [];
      return;
    }
    this.customerService.searchCustomers(term).subscribe({
      next: customers => this.customerSuggestions = customers,
      error: error => debugApiError('CustomerReceiptListComponent.onCustomerSearch', error)
    });
  }

  selectCustomer(customer: CustomerOption): void {
    this.filters.customerId = customer.id;
    this.customerSearchTerm = `${customer.customerCode || 'CUS'} - ${customer.name}`;
    this.customerSuggestions = [];
  }

  clearCustomerSelection(): void {
    this.filters.customerId = '';
    this.customerSearchTerm = '';
    this.customerSuggestions = [];
  }

  openCreateForCustomer(): void {
    this.router.navigate(['/customers/receipts/create'], {
      queryParams: this.filters.customerId ? { customerId: this.filters.customerId } : undefined
    });
  }

  exportCsv(): void {
    const csv = [
      this.exportHeaders().join(','),
      ...this.receipts.map(receipt => this.exportValues(receipt).map(value => this.csvCell(value)).join(','))
    ].join('\r\n');
    this.downloadBlob(new Blob([csv], { type: 'text/csv;charset=utf-8;' }), 'customer-receipts.csv');
  }

  exportExcel(): void {
    const rows = [this.exportHeaders(), ...this.receipts.map(receipt => this.exportValues(receipt))];
    this.downloadBlob(this.createXlsxBlob(rows), 'customer-receipts.xlsx');
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  paymentMethodClass(method?: CustomerReceiptPaymentMethod): string {
    if (method === 'BANK' || method === 'CHEQUE') {
      return 'bg-light-primary text-primary';
    }
    if (method === 'MOBILE_BANKING') {
      return 'bg-light-info text-info';
    }
    if (method === 'OTHER') {
      return 'bg-light-secondary text-secondary';
    }
    return 'bg-light-success text-success';
  }

  statusClass(status?: CustomerReceiptStatus): string {
    if (status === 'POSTED') {
      return 'bg-light-success text-success';
    }
    if (status === 'CANCELLED') {
      return 'bg-light-danger text-danger';
    }
    return 'bg-light-warning text-warning';
  }

  private exportHeaders(): string[] {
    return ['Receipt No', 'Customer', 'Receipt Date', 'Amount', 'Payment Method', 'Reference No', 'Status', 'Journal No'];
  }

  private exportValues(receipt: CustomerReceipt): string[] {
    return [
      receipt.receiptNo || '',
      receipt.customerName || '',
      receipt.receiptDate || '',
      this.formatNumber(receipt.amount),
      receipt.paymentMethod || '',
      receipt.referenceNo || '',
      receipt.status || '',
      receipt.journalNo || ''
    ];
  }

  private csvCell(value: string): string {
    return `"${String(value).replace(/"/g, '""')}"`;
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

  private createXlsxBlob(rows: string[][]): Blob {
    const sheetRows = rows.map((row, rowIndex) => `<row r="${rowIndex + 1}">${row.map((cell, colIndex) => {
      const ref = `${this.columnName(colIndex + 1)}${rowIndex + 1}`;
      return `<c r="${ref}" t="inlineStr"><is><t>${this.escapeXml(cell)}</t></is></c>`;
    }).join('')}</row>`).join('');
    const sheetXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>${sheetRows}</sheetData></worksheet>`;
    const files: { name: string; content: string }[] = [
      { name: '[Content_Types].xml', content: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>' },
      { name: '_rels/.rels', content: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>' },
      { name: 'xl/workbook.xml', content: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Customer Receipts" sheetId="1" r:id="rId1"/></sheets></workbook>' },
      { name: 'xl/_rels/workbook.xml.rels', content: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>' },
      { name: 'xl/worksheets/sheet1.xml', content: sheetXml }
    ];
    return new Blob([this.zipStore(files)], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  }

  private zipStore(files: { name: string; content: string }[]): Uint8Array {
    const encoder = new TextEncoder();
    const localParts: Uint8Array[] = [];
    const centralParts: Uint8Array[] = [];
    let offset = 0;
    for (const file of files) {
      const name = encoder.encode(file.name);
      const data = encoder.encode(file.content);
      const crc = this.crc32(data);
      const local = this.zipHeader(0x04034b50, name, data.length, crc, offset);
      localParts.push(local, data);
      centralParts.push(this.zipHeader(0x02014b50, name, data.length, crc, offset));
      offset += local.length + data.length;
    }
    const centralSize = centralParts.reduce((sum, part) => sum + part.length, 0);
    const end = new Uint8Array(22);
    const view = new DataView(end.buffer);
    view.setUint32(0, 0x06054b50, true);
    view.setUint16(8, files.length, true);
    view.setUint16(10, files.length, true);
    view.setUint32(12, centralSize, true);
    view.setUint32(16, offset, true);
    return this.concatBytes([...localParts, ...centralParts, end]);
  }

  private zipHeader(signature: number, name: Uint8Array, size: number, crc: number, offset: number): Uint8Array {
    const central = signature === 0x02014b50;
    const header = new Uint8Array((central ? 46 : 30) + name.length);
    const view = new DataView(header.buffer);
    view.setUint32(0, signature, true);
    if (central) {
      view.setUint16(4, 20, true);
      view.setUint16(6, 20, true);
      view.setUint32(16, crc, true);
      view.setUint32(20, size, true);
      view.setUint32(24, size, true);
      view.setUint16(28, name.length, true);
      view.setUint32(42, offset, true);
      header.set(name, 46);
    } else {
      view.setUint16(4, 20, true);
      view.setUint32(14, crc, true);
      view.setUint32(18, size, true);
      view.setUint32(22, size, true);
      view.setUint16(26, name.length, true);
      header.set(name, 30);
    }
    return header;
  }

  private crc32(data: Uint8Array): number {
    let crc = ~0;
    for (const byte of data) {
      crc ^= byte;
      for (let i = 0; i < 8; i++) {
        crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
      }
    }
    return ~crc >>> 0;
  }

  private concatBytes(parts: Uint8Array[]): Uint8Array {
    const total = parts.reduce((sum, part) => sum + part.length, 0);
    const output = new Uint8Array(total);
    let offset = 0;
    for (const part of parts) {
      output.set(part, offset);
      offset += part.length;
    }
    return output;
  }

  private columnName(index: number): string {
    let name = '';
    while (index > 0) {
      const mod = (index - 1) % 26;
      name = String.fromCharCode(65 + mod) + name;
      index = Math.floor((index - mod) / 26);
    }
    return name;
  }

  private escapeXml(value: string): string {
    return String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&apos;' }[char] || char));
  }
}
