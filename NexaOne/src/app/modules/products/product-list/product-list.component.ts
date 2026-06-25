import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Brand } from '../../../models/brand.model';
import { ProductCategory } from '../../../models/category.model';
import { Product, ProductStats, Status } from '../../../models/product.model';
import { ProductBrandService } from '../../../services/product-brand.service';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ProductService } from '../../../services/product.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

type SortDirection = 'asc' | 'desc';

interface ProductFilters {
  keyword: string;
  categoryId: number | null;
  brandId: number | null;
  status: Status | '';
}

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  showDeleted = false;
  categories: ProductCategory[] = [];
  brands: Brand[] = [];
  stats: ProductStats = { totalProducts: 0, activeProducts: 0, inactiveProducts: 0, productsWithoutImage: 0 };
  loading = false;
  submitError = '';

  filters: ProductFilters = { keyword: '', categoryId: null, brandId: null, status: '' };
  page = 0;
  size = 10;
  readonly pageSizes = [10, 25, 50, 100];
  totalElements = 0;
  totalPages = 0;
  sort = 'productName';
  direction: SortDirection = 'asc';
  selectedIds = new Set<number>();

  readonly exportFields = ['Product Name', 'SKU', 'Barcode', 'Category', 'Brand', 'UOM', 'Purchase Price', 'Sale Price', 'Status'];

  constructor(
    private productService: ProductService,
    private categoryService: ProductCategoryService,
    private brandService: ProductBrandService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReferenceData();
    this.loadStats();
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.submitError = '';
    if (this.showDeleted) {
      this.productService.getDeletedProducts().subscribe({
        next: products => {
          this.products = products;
          this.totalElements = products.length;
          this.totalPages = 1;
          this.page = 0;
          this.syncSelectionWithPage();
          this.loading = false;
        },
        error: error => {
          this.products = [];
          this.loading = false;
          this.submitError = extractApiErrorMessage(error, 'Products could not be loaded.');
          debugApiError('ProductListComponent.loadProducts', error);
        }
      });
      return;
    }

    this.productService.getProductPage({
      keyword: this.filters.keyword,
      categoryId: this.filters.categoryId,
      brandId: this.filters.brandId,
      status: this.filters.status,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    }).subscribe({
      next: pageData => {
        this.products = pageData.content;
        this.totalElements = pageData.totalElements;
        this.totalPages = pageData.totalPages;
        this.page = pageData.page;
        this.size = pageData.size;
        this.syncSelectionWithPage();
        this.loading = false;
      },
      error: error => {
        this.products = [];
        this.loading = false;
        this.submitError = extractApiErrorMessage(error, 'Products could not be loaded.');
        debugApiError('ProductListComponent.loadProducts', error);
      }
    });
  }

  loadStats(): void {
    this.productService.getProductStats().subscribe({
      next: stats => this.stats = stats,
      error: error => debugApiError('ProductListComponent.loadStats', error)
    });
  }

  loadReferenceData(): void {
    this.categoryService.getAllCategories().subscribe({
      next: categories => this.categories = categories,
      error: error => debugApiError('ProductListComponent.loadCategories', error)
    });
    this.brandService.getAllBrands().subscribe({
      next: brands => this.brands = brands,
      error: error => debugApiError('ProductListComponent.loadBrands', error)
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.selectedIds.clear();
    this.loadProducts();
  }

  resetFilters(): void {
    this.filters = { keyword: '', categoryId: null, brandId: null, status: '' };
    this.page = 0;
    this.selectedIds.clear();
    this.loadProducts();
  }

  toggleDeletedView(): void {
    this.showDeleted = !this.showDeleted;
    this.page = 0;
    this.selectedIds.clear();
    this.loadProducts();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadProducts();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = 'asc';
    }
    this.loadProducts();
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
    this.loadProducts();
  }

  viewProduct(product: Product): void {
    if (product.id) {
      this.router.navigate(['/products/details', product.id]);
    }
  }

  editProduct(product: Product): void {
    if (product.id && !this.showDeleted) {
      this.router.navigate(['/products/edit-product', product.id]);
    }
  }

  deleteProduct(id?: number): void {
    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          this.selectedIds.delete(id);
          this.loadProducts();
          this.loadStats();
        },
        error: (error) => {
          this.submitError = extractApiErrorMessage(error, 'Delete request failed.');
          debugApiError('ProductListComponent.deleteProduct', error);
        }
      });
    }
  }

  restoreProduct(id?: number): void {
    if (!id) {
      return;
    }
    if (confirm('Restore this product?')) {
      this.productService.restoreProduct(id).subscribe({
        next: () => this.loadProducts(),
        error: (error) => {
          this.submitError = extractApiErrorMessage(error, 'Restore request failed.');
          debugApiError('ProductListComponent.restoreProduct', error);
        }
      });
    }
  }

  toggleProduct(product: Product, checked: boolean): void {
    if (!product.id) {
      return;
    }
    checked ? this.selectedIds.add(product.id) : this.selectedIds.delete(product.id);
  }

  togglePageSelection(checked: boolean): void {
    this.products.forEach(product => {
      if (product.id) {
        checked ? this.selectedIds.add(product.id) : this.selectedIds.delete(product.id);
      }
    });
  }

  isSelected(product: Product): boolean {
    return !!product.id && this.selectedIds.has(product.id);
  }

  get allPageSelected(): boolean {
    return this.products.length > 0 && this.products.every(product => product.id && this.selectedIds.has(product.id));
  }

  get selectedProducts(): Product[] {
    return this.products.filter(product => product.id && this.selectedIds.has(product.id));
  }

  get exportRows(): Product[] {
    return this.selectedProducts.length ? this.selectedProducts : this.products;
  }

  bulkActivate(): void {
    this.bulkUpdateStatus('ACTIVE');
  }

  bulkDeactivate(): void {
    this.bulkUpdateStatus('INACTIVE');
  }

  exportCsv(): void {
    const rows = this.exportRows;
    const csv = [
      this.exportFields.join(','),
      ...rows.map(product => this.exportValues(product).map(value => this.csvCell(value)).join(','))
    ].join('\r\n');
    this.downloadBlob(new Blob([csv], { type: 'text/csv;charset=utf-8;' }), 'products.csv');
  }

  exportExcel(): void {
    const rows = [this.exportFields, ...this.exportRows.map(product => this.exportValues(product))];
    const blob = this.createXlsxBlob(rows);
    this.downloadBlob(blob, 'products.xlsx');
  }

  printProduct(product: Product): void {
    this.openPrintWindow([product], 'single');
  }

  printSelected(): void {
    if (!this.selectedProducts.length) {
      return;
    }
    this.openPrintWindow(this.selectedProducts, 'sheet');
  }

  printLabelSheet(): void {
    this.openPrintWindow(this.exportRows, 'sheet');
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  productImageUrl(product: Product): string {
    return this.productService.resolveImageUrl(product.imageUrl);
  }

  private bulkUpdateStatus(status: Status): void {
    const ids = Array.from(this.selectedIds);
    if (!ids.length) {
      return;
    }
    this.productService.updateStatusBulk(ids, status).subscribe({
      next: () => {
        this.selectedIds.clear();
        this.loadProducts();
        this.loadStats();
      },
      error: error => {
        this.submitError = extractApiErrorMessage(error, 'Bulk update failed.');
        debugApiError('ProductListComponent.bulkUpdateStatus', error);
      }
    });
  }

  private syncSelectionWithPage(): void {
    const visibleIds = new Set(this.products.map(product => product.id).filter((id): id is number => !!id));
    Array.from(this.selectedIds).forEach(id => {
      if (!visibleIds.has(id)) {
        this.selectedIds.delete(id);
      }
    });
  }

  private exportValues(product: Product): string[] {
    return [
      product.productName || '',
      product.sku || '',
      product.barcode || '',
      product.categoryName || '',
      product.brandName || '',
      product.uomName || '',
      this.formatNumber(product.purchasePrice),
      this.formatNumber(product.salePrice),
      product.status || ''
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

  private openPrintWindow(products: Product[], mode: 'single' | 'sheet'): void {
    const labels = products.map(product => this.labelHtml(product)).join('');
    const html = `<!doctype html><html><head><title>Product Labels</title><style>
      @page{size:A4;margin:10mm}
      *{box-sizing:border-box}
      body{font-family:Arial,sans-serif;margin:0;color:#111}
      .sheet{display:grid;grid-template-columns:repeat(3,1fr);gap:8mm}
      .single{display:flex;align-items:flex-start;justify-content:center;padding-top:24mm}
      .label{border:1px solid #111;border-radius:4px;padding:7px;min-height:38mm;break-inside:avoid}
      .name{font-size:12px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
      .meta{font-size:10px;margin-top:2px}
      .price{font-size:12px;font-weight:700;margin-top:3px}
      .barcode svg{width:100%;height:40px;margin-top:4px}
      @media print{button{display:none}}
    </style></head><body><main class="${mode === 'single' ? 'single' : 'sheet'}">${labels}</main><script>window.onload=function(){window.print();}</script></body></html>`;
    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) {
      return;
    }
    printWindow.document.open();
    printWindow.document.write(html);
    printWindow.document.close();
  }

  private labelHtml(product: Product): string {
    const barcodeValue = this.barcodeValue(product);
    return `<section class="label">
      <div class="name">${this.escapeHtml(product.productName || '')}</div>
      <div class="meta">SKU: ${this.escapeHtml(product.sku || '')}</div>
      <div class="barcode">${this.code39Svg(barcodeValue)}</div>
      <div class="meta">${this.escapeHtml(barcodeValue)}</div>
      <div class="price">Price: ${this.formatNumber(product.salePrice)}</div>
    </section>`;
  }

  private barcodeValue(product: Product): string {
    const raw = (product.barcode || product.sku || product.productCode || 'PRODUCT').toUpperCase();
    const cleaned = raw.replace(/[^0-9A-Z\-. $/+%]/g, '');
    return cleaned || 'PRODUCT';
  }

  private code39Svg(value: string): string {
    const patterns: { [key: string]: string } = {
      '0': '101001101101', '1': '110100101011', '2': '101100101011', '3': '110110010101',
      '4': '101001101011', '5': '110100110101', '6': '101100110101', '7': '101001011011',
      '8': '110100101101', '9': '101100101101', 'A': '110101001011', 'B': '101101001011',
      'C': '110110100101', 'D': '101011001011', 'E': '110101100101', 'F': '101101100101',
      'G': '101010011011', 'H': '110101001101', 'I': '101101001101', 'J': '101011001101',
      'K': '110101010011', 'L': '101101010011', 'M': '110110101001', 'N': '101011010011',
      'O': '110101101001', 'P': '101101101001', 'Q': '101010110011', 'R': '110101011001',
      'S': '101101011001', 'T': '101011011001', 'U': '110010101011', 'V': '100110101011',
      'W': '110011010101', 'X': '100101101011', 'Y': '110010110101', 'Z': '100110110101',
      '-': '100101011011', '.': '110010101101', ' ': '100110101101', '$': '100100100101',
      '/': '100100101001', '+': '100101001001', '%': '101001001001', '*': '100101101101'
    };
    const encoded = `*${value}*`;
    let x = 0;
    const bars: string[] = [];
    for (const char of encoded) {
      const pattern = patterns[char] || patterns['-'];
      for (const bit of pattern) {
        if (bit === '1') {
          bars.push(`<rect x="${x}" y="0" width="1" height="40"></rect>`);
        }
        x += 1;
      }
      x += 1;
    }
    return `<svg viewBox="0 0 ${x} 40" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">${bars.join('')}</svg>`;
  }

  private escapeHtml(value: string): string {
    return value.replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char] || char));
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
      { name: 'xl/workbook.xml', content: '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Products" sheetId="1" r:id="rId1"/></sheets></workbook>' },
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
