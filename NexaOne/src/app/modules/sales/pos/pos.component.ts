import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ProductCategory } from '../../../models/category.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { Product } from '../../../models/product.model';
import { SalesCustomer } from '../../../models/sales-common.model';
import { Stock } from '../../../models/stock.model';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { PosPaymentMethod, PosSaleRequest, SalesPosService } from '../../../services/sales-pos.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { NotificationService } from '../../../shared/services/notification.service';
import { AuthService } from '../../auth/auth.service';

interface PosCartLine {
  product: Product;
  quantity: number;
  unitPrice: number;
}

@Component({
  selector: 'app-pos',
  templateUrl: './pos.component.html',
  styleUrls: ['./pos.component.css']
})
export class PosComponent implements OnInit, AfterViewInit {
  @ViewChild('barcodeInput') barcodeInput?: ElementRef<HTMLInputElement>;

  products: Product[] = [];
  categories: ProductCategory[] = [];
  customers: SalesCustomer[] = [];
  warehouses: InventoryWarehouse[] = [];
  stocks: Stock[] = [];
  cart: PosCartLine[] = [];
  loading = false;
  searchTerm = '';
  barcodeValue = '';
  selectedCategoryId: number | null = null;
  selectedCustomerId: number | null = null;
  selectedWarehouseId: number | null = null;
  paymentMethod: PosPaymentMethod = 'CASH';
  paidAmount = 0;
  discountAmount = 0;
  paymentReferenceNo = '';
  notes = '';
  message = '';
  warningMessage = '';
  stockAvailable = false;
  submitting = false;

  readonly paymentMethods: Array<{ value: PosPaymentMethod; label: string }> = [
    { value: 'CASH', label: 'Cash' },
    { value: 'CARD', label: 'Card' },
    { value: 'MOBILE_BANKING', label: 'Mobile Banking' },
    { value: 'BANK', label: 'Bank' },
    { value: 'DUE', label: 'Due' },
    { value: 'OTHER', label: 'Other' }
  ];

  constructor(
    private productService: ProductService,
    private categoryService: ProductCategoryService,
    private customerService: SalesCustomerService,
    private warehouseService: InventoryWarehouseService,
    private stockService: InventoryStockService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    public posService: SalesPosService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.focusBarcode());
  }

  get filteredProducts(): Product[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.products.filter(product => product.status !== 'INACTIVE'
      && (!this.selectedCategoryId || product.categoryId === this.selectedCategoryId)
      && (!keyword
        || product.productName.toLowerCase().includes(keyword)
        || (product.sku || '').toLowerCase().includes(keyword)
        || (product.barcode || '').toLowerCase().includes(keyword)
        || (product.productCode || '').toLowerCase().includes(keyword)));
  }

  get subtotal(): number {
    return this.cart.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0);
  }

  get taxAmount(): number {
    const discounts = this.allocatedDiscounts();
    return this.cart.reduce((sum, line, index) => {
      const taxable = Math.max(line.quantity * line.unitPrice - discounts[index], 0);
      return sum + taxable * Number(line.product.taxPercentage || 0) / 100;
    }, 0);
  }

  get normalizedDiscount(): number {
    return Math.min(Math.max(Number(this.discountAmount || 0), 0), this.subtotal);
  }

  get grandTotal(): number {
    return Math.max(this.subtotal - this.normalizedDiscount + this.taxAmount, 0);
  }

  get changeDue(): number {
    return this.paymentMethod === 'CASH' ? Math.max(Number(this.paidAmount || 0) - this.grandTotal, 0) : 0;
  }

  get dueAmount(): number {
    return Math.max(this.grandTotal - Math.max(Number(this.paidAmount || 0), 0), 0);
  }

  get selectedCustomer(): SalesCustomer | undefined {
    return this.customers.find(customer => customer.id === this.selectedCustomerId);
  }

  get validationErrors(): string[] {
    const errors: string[] = [];
    if (!this.cart.length) errors.push('Add at least one product to the cart.');
    if (this.cart.some(line => !Number.isFinite(Number(line.quantity)) || line.quantity <= 0)) errors.push('Every cart quantity must be positive.');
    if (this.cart.some(line => !Number.isFinite(Number(line.unitPrice)) || line.unitPrice < 0)) errors.push('Every cart price must be a valid non-negative number.');
    if (!this.selectedWarehouseId) errors.push('Select a warehouse.');
    if (!this.selectedCustomerId) errors.push('Select a customer.');
    if (!Number.isFinite(Number(this.paidAmount)) || Number(this.paidAmount) < 0) errors.push('Paid amount must be a valid non-negative number.');
    if (!Number.isFinite(Number(this.discountAmount)) || Number(this.discountAmount) < 0 || Number(this.discountAmount) > this.subtotal) {
      errors.push('Discount must be between zero and the cart subtotal.');
    }
    if (this.paymentMethod === 'DUE' && (!this.selectedCustomerId || this.isWalkIn(this.selectedCustomer))) {
      errors.push('Due sales require a known non-walk-in customer until the backend business rule is defined.');
    }
    return errors;
  }

  loadData(): void {
    this.loading = true;
    let pending = 4;
    const done = () => { pending -= 1; if (pending === 0) this.loading = false; };

    this.productService.getAllProducts().subscribe({
      next: products => { this.products = products; done(); },
      error: error => { this.warningMessage = 'Products could not be loaded.'; debugApiError('PosComponent.products', error); done(); }
    });
    this.categoryService.getAllCategories().subscribe({
      next: categories => { this.categories = categories.filter(category => category.status === 'ACTIVE'); done(); },
      error: error => { debugApiError('PosComponent.categories', error); done(); }
    });
    this.customerService.getAllCustomers().subscribe({
      next: customers => {
        this.customers = customers;
        this.selectedCustomerId = customers.find(customer => this.isWalkIn(customer))?.id || null;
        done();
      },
      error: error => { debugApiError('PosComponent.customers', error); done(); }
    });
    this.warehouseService.getAllWarehouses().subscribe({
      next: warehouses => {
        this.warehouses = warehouses.filter(warehouse => warehouse.active !== false);
        this.selectedWarehouseId = this.warehouses[0]?.id || null;
        this.loadStock();
        done();
      },
      error: error => { debugApiError('PosComponent.warehouses', error); done(); }
    });
  }

  loadStock(): void {
    this.stockAvailable = false;
    this.stocks = [];
    if (!this.authService.hasPermission('INVENTORY_VIEW')) return;
    this.stockService.getAllStock().subscribe({
      next: stocks => { this.stocks = stocks; this.stockAvailable = true; },
      error: error => debugApiError('PosComponent.stock', error)
    });
  }

  scanBarcode(): void {
    const code = this.barcodeValue.trim().toLowerCase();
    if (!code) return;
    const product = this.products.find(item => (item.barcode || '').toLowerCase() === code
      || (item.sku || '').toLowerCase() === code
      || (item.productCode || '').toLowerCase() === code);
    if (product) {
      this.addToCart(product);
      this.warningMessage = '';
    } else {
      this.warningMessage = `No active product matched barcode/SKU “${this.barcodeValue.trim()}”.`;
    }
    this.barcodeValue = '';
    this.focusBarcode();
  }

  addToCart(product: Product): void {
    if (product.status === 'INACTIVE') return;
    const knownStock = this.stockFor(product);
    if (product.type !== 'SERVICE' && knownStock !== null && knownStock <= 0) {
      this.warningMessage = `${product.productName} has zero stock in the selected warehouse.`;
      return;
    }
    const existing = this.cart.find(line => line.product.id === product.id);
    if (existing) {
      this.changeQuantity(existing, 1);
    } else {
      this.cart.push({ product, quantity: 1, unitPrice: Number(product.salePrice || 0) });
    }
  }

  changeQuantity(line: PosCartLine, change: number): void {
    const next = line.quantity + change;
    if (next <= 0) return;
    const knownStock = this.stockFor(line.product);
    if (line.product.type !== 'SERVICE' && knownStock !== null && next > knownStock) {
      this.warningMessage = `Only ${knownStock} ${line.product.productName} available in this warehouse.`;
      return;
    }
    line.quantity = next;
  }

  setQuantity(line: PosCartLine, value: number): void {
    const quantity = Number(value);
    if (!Number.isFinite(quantity) || quantity <= 0) {
      line.quantity = 1;
      this.warningMessage = 'Quantity must be positive.';
      return;
    }
    const knownStock = this.stockFor(line.product);
    line.quantity = knownStock !== null && line.product.type !== 'SERVICE' ? Math.min(quantity, knownStock) : quantity;
  }

  removeLine(line: PosCartLine): void {
    this.cart = this.cart.filter(item => item !== line);
  }

  clearCart(): void {
    if (this.cart.length && !window.confirm('Clear all items from the cart?')) return;
    this.resetCart();
  }

  onWarehouseChange(): void {
    this.warningMessage = '';
    this.cart.forEach(line => {
      const knownStock = this.stockFor(line.product);
      if (knownStock !== null && line.quantity > knownStock) {
        this.warningMessage = 'Some cart quantities exceed stock in the newly selected warehouse.';
      }
    });
  }

  onPaymentMethodChange(): void {
    this.paidAmount = this.paymentMethod === 'DUE' ? 0 : this.grandTotal;
  }

  normalizePaidAmount(): void {
    if (!Number.isFinite(Number(this.paidAmount))) this.paidAmount = 0;
  }

  holdSale(): void {
    this.message = 'Hold Sale is a Phase 3 placeholder. No sale or draft was saved.';
  }

  completeSale(printAfterCompletion: boolean): void {
    if (!this.posService.completionAvailable || this.validationErrors.length || this.submitting) return;
    const discounts = this.allocatedDiscounts();
    const request: PosSaleRequest = {
      customerId: Number(this.selectedCustomerId),
      warehouseId: Number(this.selectedWarehouseId),
      saleDate: this.localDateTime(),
      items: this.cart.map((line, index) => ({
        productId: Number(line.product.id),
        quantity: line.quantity,
        unitPrice: line.unitPrice,
        discount: discounts[index],
        tax: Math.max(line.quantity * line.unitPrice - discounts[index], 0) * Number(line.product.taxPercentage || 0) / 100
      })),
      payment: {
        paymentMethod: this.paymentMethod,
        paidAmount: Math.min(Math.max(Number(this.paidAmount || 0), 0), this.grandTotal),
        referenceNo: this.paymentReferenceNo.trim() || undefined
      },
      notes: this.notes.trim() || undefined
    };
    this.submitting = true;
    this.warningMessage = '';
    this.posService.completeSale(request).subscribe({
      next: completed => {
        this.submitting = false;
        this.notificationService.success(`Sale ${completed.invoiceNo} completed successfully.`);
        this.resetCart();
        this.router.navigate(['/sales', completed.invoiceId, 'view'], {
          queryParams: printAfterCompletion ? { print: true } : undefined
        });
      },
      error: error => {
        this.submitting = false;
        this.warningMessage = extractApiErrorMessage(error, 'POS completion failed. No sale was created.');
        debugApiError('PosComponent.completeSale', error);
      }
    });
  }

  stockFor(product: Product): number | null {
    if (!this.stockAvailable || !product.id || !this.selectedWarehouseId) return null;
    return Number(this.stocks.find(stock => stock.productId === product.id && stock.warehouseId === this.selectedWarehouseId)?.quantity || 0);
  }

  imageUrl(product: Product): string {
    return this.productService.resolveImageUrl(product.imageUrl || product.imagePath);
  }

  focusBarcode(): void {
    this.barcodeInput?.nativeElement.focus();
  }

  private isWalkIn(customer?: SalesCustomer): boolean {
    return !!customer && /walk[\s-]*in/i.test(customer.name || '');
  }

  private allocatedDiscounts(): number[] {
    if (!this.cart.length || this.subtotal <= 0 || this.normalizedDiscount <= 0) {
      return this.cart.map(() => 0);
    }
    let allocated = 0;
    return this.cart.map((line, index) => {
      if (index === this.cart.length - 1) return this.roundMoney(this.normalizedDiscount - allocated);
      const share = this.roundMoney(this.normalizedDiscount * (line.quantity * line.unitPrice) / this.subtotal);
      allocated += share;
      return share;
    });
  }

  private resetCart(): void {
    this.cart = [];
    this.discountAmount = 0;
    this.paidAmount = 0;
    this.paymentReferenceNo = '';
    this.notes = '';
    this.message = '';
  }

  private localDateTime(): string {
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60000;
    return new Date(now.getTime() - offset).toISOString().slice(0, 19);
  }

  private roundMoney(value: number): number {
    return Math.round((value + Number.EPSILON) * 100) / 100;
  }
}
