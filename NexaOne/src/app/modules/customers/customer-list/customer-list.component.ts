import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Customer } from '../../../models/customer.model';
import { Status } from '../../../models/product.model';
import { CustomerService } from '../../../services/customer.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.css']
})
export class CustomerListComponent implements OnInit {
  customers: Customer[] = [];
  showDeleted = false;
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    status: '' as Status | ''
  };

  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  sort = 'createdAt';
  direction: SortDirection = 'desc';

  readonly pageSizes = [10, 25, 50, 100];
  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];
  readonly exportFields = [
    'Customer Code',
    'Name',
    'Phone',
    'Email',
    'Status',
    'Due Balance',
    'Credit Limit',
    'Opening Balance',
    'Current Balance'
  ];

  constructor(
    private customerService: CustomerService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.errorMessage = '';
    if (this.showDeleted) {
      this.customerService.getDeletedCustomers().subscribe({
        next: customers => {
          this.customers = customers;
          this.totalElements = customers.length;
          this.totalPages = 1;
          this.page = 0;
          this.loading = false;
        },
        error: error => {
          this.customers = [];
          this.loading = false;
          this.errorMessage = extractApiErrorMessage(error, 'Customers could not be loaded.');
          debugApiError('CustomerListComponent.loadCustomers', error);
        }
      });
      return;
    }

    this.customerService.getCustomerPage({
      keyword: this.filters.keyword,
      status: this.filters.status,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    }).subscribe({
      next: pageData => {
        this.customers = pageData.content;
        this.totalElements = pageData.totalElements;
        this.totalPages = pageData.totalPages;
        this.page = pageData.page;
        this.size = pageData.size;
        this.loading = false;
      },
      error: error => {
        this.customers = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Customers could not be loaded.');
        debugApiError('CustomerListComponent.loadCustomers', error);
      }
    });
  }

  search(): void {
    this.page = 0;
    this.loadCustomers();
  }

  resetFilters(): void {
    this.filters = {
      keyword: '',
      status: ''
    };
    this.page = 0;
    this.sort = 'createdAt';
    this.direction = 'desc';
    this.loadCustomers();
  }

  toggleDeletedView(): void {
    this.showDeleted = !this.showDeleted;
    this.page = 0;
    this.loadCustomers();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadCustomers();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = 'asc';
    }
    this.loadCustomers();
  }

  sortIcon(column: string): string {
    if (this.sort !== column) {
      return 'bi-arrow-down-up';
    }
    return this.direction === 'asc' ? 'bi-sort-alpha-down' : 'bi-sort-alpha-up';
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) {
      return;
    }
    this.page = page;
    this.loadCustomers();
  }

  createCustomer(): void {
    this.router.navigate(['/customers/create']);
  }

  viewCustomer(customer: Customer): void {
    if (customer.id) {
      this.router.navigate(['/customers/details', customer.id]);
    }
  }

  editCustomer(customer: Customer): void {
    if (customer.id && !this.showDeleted) {
      this.router.navigate(['/customers/edit', customer.id]);
    }
  }

  deleteCustomer(customer: Customer): void {
    if (!customer.id) {
      return;
    }

    if (confirm(`Are you sure you want to delete customer "${customer.name}"?`)) {
      this.customerService.deleteCustomer(customer.id).subscribe({
        next: () => this.loadCustomers(),
        error: (error) => {
          this.errorMessage = extractApiErrorMessage(error, 'Delete request failed.');
          debugApiError('CustomerListComponent.deleteCustomer', error);
        }
      });
    }
  }

  restoreCustomer(customer: Customer): void {
    if (!customer.id || !confirm(`Restore customer "${customer.name}"?`)) {
      return;
    }
    this.customerService.restoreCustomer(customer.id).subscribe({
      next: () => this.loadCustomers(),
      error: (error) => {
        this.errorMessage = extractApiErrorMessage(error, 'Restore request failed.');
        debugApiError('CustomerListComponent.restoreCustomer', error);
      }
    });
  }

  exportCsv(): void {
    const csv = [
      this.exportFields.join(','),
      ...this.customers.map(customer => this.exportValues(customer).map(value => this.csvCell(value)).join(','))
    ].join('\r\n');
    this.downloadBlob(new Blob([csv], { type: 'text/csv;charset=utf-8;' }), 'customers.csv');
  }

  exportExcel(): void {
    const rows = [this.exportFields, ...this.customers.map(customer => this.exportValues(customer))];
    this.downloadBlob(this.createXlsxBlob(rows), 'customers.xlsx');
  }

  statusClass(status?: Status): string {
    if (status === 'ACTIVE') {
      return 'bg-light-success text-success';
    }
    if (status === 'INACTIVE') {
      return 'bg-light-danger text-danger';
    }
    if (status === 'DRAFT') {
      return 'bg-light-warning text-warning';
    }
    return 'bg-light text-dark';
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  private exportValues(customer: Customer): string[] {
    return [
      customer.customerCode || '',
      customer.name || '',
      customer.phone || '',
      customer.email || '',
      customer.status || '',
      this.formatNumber(customer.dueBalance),
      this.formatNumber(customer.creditLimit),
      this.formatNumber(customer.openingBalance),
      this.formatNumber(customer.currentBalance)
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
      { name: 'xl/workbook.xml', content: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Customers" sheetId="1" r:id="rId1"/></sheets></workbook>' },
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
