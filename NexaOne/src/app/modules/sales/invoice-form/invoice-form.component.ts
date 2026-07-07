import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BrowserMultiFormatReader, IScannerControls } from '@zxing/browser';
import { Result } from '@zxing/library';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { Product } from '../../../models/product.model';
import { PaymentStatus, SalesCustomer, SalesInvoiceLineItem } from '../../../models/sales-common.model';
import { SalesInvoice } from '../../../models/sales-invoice.model';
import { SALES_PAYMENT_METHODS, SalesPaymentInput, SalesPaymentMethod, normalizeSalesPayment, previewPaymentStatus, toReceiptPaymentMethod } from '../../../models/sales-payment.model';
import { SalesOrder } from '../../../models/sales-order.model';
import { Stock } from '../../../models/stock.model';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { SalesOrderService } from '../../../services/sales-order.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

type DiscountType = 'PERCENTAGE' | 'FIXED';

@Component({
  selector: 'app-sales-invoice-form',
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.css']
})
export class InvoiceFormComponent implements OnInit, OnDestroy {
  @ViewChild('scannerVideo') scannerVideo?: ElementRef<HTMLVideoElement>;
  form: FormGroup;
  mode: 'create' | 'edit' | 'details' = 'create';
  invoice: SalesInvoice | null = null;
  customers: SalesCustomer[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  orders: SalesOrder[] = [];
  stocks: Stock[] = [];
  stockAvailable = false;
  stockWarning = '';
  customerSearchTerm = '';
  customerMenuOpen = false;
  showInlineCustomerForm = false;
  barcodeScan = '';
  productSearchTerms: string[] = [];
  productMenusOpen: boolean[] = [];
  scannerOpen = false;
  scannerMessage = '';
  private scannerReader?: BrowserMultiFormatReader;
  private scannerControls?: IScannerControls;
  loading = false;
  submitting = false;
  errorMessage = '';
  readonly paymentMethods = SALES_PAYMENT_METHODS;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private invoiceService: SalesInvoiceService,
    private orderService: SalesOrderService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private stockService: InventoryStockService,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      orderId: [null],
      customerId: [null],
      warehouseId: [null, Validators.required],
      saleDate: [this.today(), Validators.required],
      notes: ['', Validators.maxLength(500)],
      discountType: ['PERCENTAGE', Validators.required],
      discountValue: [0, [Validators.required, Validators.min(0)]],
      vatPercentage: [0, [Validators.required, Validators.min(0)]],
      adjustmentAmount: [0, Validators.required],
      newCustomer: this.fb.group({
        name: ['', Validators.maxLength(255)],
        phone: ['', Validators.maxLength(100)],
        email: ['', [Validators.email, Validators.maxLength(255)]],
        address: ['']
      }),
      payment: this.fb.group({
        paidAmount: [0, [Validators.required, Validators.min(0)]],
        paymentMethod: ['CASH', Validators.required],
        reference: ['', Validators.maxLength(255)],
        notes: ['', Validators.maxLength(500)]
      }),
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.mode = this.route.snapshot.data['mode'] || 'create';
    const paymentAmount = Number(this.route.snapshot.queryParamMap.get('paymentAmount') || 0);
    const paymentMethod = this.route.snapshot.queryParamMap.get('paymentMethod') as SalesPaymentMethod | null;
    this.form.get('payment')?.patchValue({
      paidAmount: Number.isFinite(paymentAmount) && paymentAmount > 0 ? paymentAmount : 0,
      paymentMethod: paymentMethod && SALES_PAYMENT_METHODS.some(method => method.value === paymentMethod) ? paymentMethod : 'CASH',
      reference: this.route.snapshot.queryParamMap.get('paymentReference') || '',
      notes: this.route.snapshot.queryParamMap.get('paymentNotes') || ''
    });
    this.loadReferenceData();
    const id = Number(this.route.snapshot.paramMap.get('id') || 0);
    if (id) this.loadInvoice(id);
  }

  ngOnDestroy(): void {
    this.stopScanner();
  }

  get items(): FormArray { return this.form.get('items') as FormArray; }
  get grossAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('quantity')?.value || 0) * Number(row.get('unitPrice')?.value || 0), 0); }
  get totalAmount(): number { return this.roundMoney(this.items.controls.reduce((sum, _, index) => sum + this.rowSubtotal(index), 0)); }
  get discountAmount(): number {
    const type = (this.form.get('discountType')?.value || 'PERCENTAGE') as DiscountType;
    const value = Number(this.form.get('discountValue')?.value || 0);
    const discount = type === 'PERCENTAGE' ? this.totalAmount * Math.min(value, 100) / 100 : value;
    return this.roundMoney(Math.min(Math.max(discount, 0), this.totalAmount));
  }
  get taxAmount(): number {
    const taxable = Math.max(this.totalAmount - this.discountAmount, 0);
    const percentage = Math.max(Number(this.form.get('vatPercentage')?.value || 0), 0);
    return this.roundMoney(taxable * percentage / 100);
  }
  get adjustmentAmount(): number { return this.roundMoney(Number(this.form.get('adjustmentAmount')?.value || 0)); }
  get netTotal(): number { return this.roundMoney(Math.max(this.totalAmount - this.discountAmount + this.taxAmount + this.adjustmentAmount, 0)); }
  get previewDueAmount(): number { return this.roundMoney(Math.max(this.netTotal - this.paymentInput.paidAmount, 0)); }
  get dueAmount(): number | null { return this.invoice ? Number(this.invoice.dueAmount || 0) : this.previewDueAmount; }
  get paymentStatus(): PaymentStatus | null { return this.invoice?.paymentStatus ?? null; }
  get paymentStatusPreview(): PaymentStatus { return previewPaymentStatus(this.paymentInput, this.netTotal); }
  get filteredCustomers(): SalesCustomer[] {
    const keyword = this.customerSearchTerm.trim().toLowerCase();
    if (!keyword) return this.customers.slice(0, 8);
    return this.customers.filter(customer => customer.name.toLowerCase().includes(keyword)
      || (customer.customerCode || '').toLowerCase().includes(keyword)
      || (customer.phone || '').toLowerCase().includes(keyword)).slice(0, 8);
  }

  addItem(item?: Partial<SalesInvoiceLineItem>): void {
    this.items.push(this.fb.group({
      productId: [item?.productId ?? null, Validators.required],
      quantity: [item?.quantity ?? 1, [Validators.required, Validators.min(.01)]],
      unitPrice: [item?.unitPrice ?? 0, [Validators.required, Validators.min(0)]],
      discount: [item?.discount ?? 0, [Validators.required, Validators.min(0)]],
      tax: [item?.tax ?? 0, [Validators.required, Validators.min(0)]]
    }));
    const product = item?.productId ? this.products.find(current => current.id === Number(item.productId)) : null;
    this.productSearchTerms.push(item?.productName || product?.productName || '');
    this.productMenusOpen.push(false);
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
      this.productSearchTerms.splice(index, 1);
      this.productMenusOpen.splice(index, 1);
    }
  }

  rowSubtotal(index: number): number {
    const row = this.items.at(index);
    return Number(row.get('quantity')?.value || 0) * Number(row.get('unitPrice')?.value || 0)
      - Number(row.get('discount')?.value || 0) + Number(row.get('tax')?.value || 0);
  }

  onProductChange(index: number): void {
    const row = this.items.at(index);
    const product = this.products.find(item => item.id === Number(row.get('productId')?.value));
    this.stockWarning = '';
    if (product) {
      row.patchValue({ unitPrice: Number(product.salePrice || 0) });
      this.warnForStock(product);
    }
  }

  onProductInput(index: number, value: string): void {
    this.productSearchTerms[index] = value;
    this.productMenusOpen[index] = true;
    const selected = this.products.find(product => product.id === Number(this.items.at(index).get('productId')?.value));
    if (!selected || selected.productName.toLowerCase() !== value.trim().toLowerCase()) {
      this.items.at(index).patchValue({ productId: null, unitPrice: 0, discount: 0, tax: 0 });
    }
  }

  selectProduct(index: number, product: Product): void {
    if (!product.id || !this.isProductAvailable(product)) return;
    const row = this.items.at(index);
    row.patchValue({
      productId: product.id,
      unitPrice: Number(product.salePrice || 0),
      discount: Number(row.get('discount')?.value || 0),
      tax: Number(row.get('tax')?.value || 0)
    });
    this.productSearchTerms[index] = product.productName;
    this.productMenusOpen[index] = false;
    this.stockWarning = '';
  }

  filteredProducts(index: number): Product[] {
    const keyword = (this.productSearchTerms[index] || '').trim().toLowerCase();
    return this.availableProducts()
      .filter(product => !keyword || product.productName.toLowerCase().includes(keyword))
      .slice(0, 10);
  }

  closeProductMenu(index: number): void {
    setTimeout(() => this.productMenusOpen[index] = false, 150);
  }

  onCustomerInput(value: string): void {
    this.customerSearchTerm = value;
    this.customerMenuOpen = true;
    const term = value.trim().toLowerCase();
    if (this.showInlineCustomerForm) return;
    if (!term) {
      const walkIn = this.customers.find(customer => /walk[\s-]*in/i.test(customer.name || ''));
      this.form.patchValue({ customerId: walkIn?.id || null });
      return;
    }
    const exact = this.customers.find(customer => customer.name.toLowerCase() === term
      || (customer.customerCode || '').toLowerCase() === term);
    this.form.patchValue({ customerId: exact?.id || null });
  }

  selectCustomer(customer: SalesCustomer): void {
    this.form.patchValue({ customerId: customer.id });
    this.customerSearchTerm = customer.name;
    this.customerMenuOpen = false;
    this.showInlineCustomerForm = false;
    this.form.get('newCustomer')?.reset({ name: '', phone: '', email: '', address: '' });
  }

  createCustomer(): void {
    this.showInlineCustomerForm = true;
    this.form.patchValue({ customerId: null });
    this.form.get('newCustomer.name')?.setValue(this.customerSearchTerm.trim());
    this.customerMenuOpen = false;
  }

  onWarehouseChange(): void {
    const warehouseId = Number(this.form.get('warehouseId')?.value || 0);
    if (warehouseId) localStorage.setItem(this.warehouseStorageKey(), String(warehouseId));
    this.stockWarning = '';
    this.items.controls.forEach((row, index) => {
      const product = this.products.find(item => item.id === Number(row.get('productId')?.value));
      if (product && !this.isProductAvailable(product)) {
        row.patchValue({ productId: null, unitPrice: 0, discount: 0, tax: 0 });
        this.productSearchTerms[index] = '';
      }
    });
  }

  onOrderChange(): void {
    const order = this.orders.find(item => item.id === Number(this.form.get('orderId')?.value));
    if (!order) return;
    this.clearItems();
    this.form.patchValue({ customerId: order.customerId, warehouseId: order.warehouseId });
    this.customerSearchTerm = this.customers.find(customer => customer.id === order.customerId)?.name || order.customerName || '';
    this.onWarehouseChange();
    (order.items || []).forEach(item => this.addItem({
      productId: item.productId, quantity: item.quantity, unitPrice: item.unitPrice,
      discount: 0, tax: 0, subtotal: item.subtotal
    }));
    if (!this.items.length) this.addItem();
  }

  scanBarcode(): void {
    const code = this.barcodeScan.trim().toLowerCase();
    this.errorMessage = '';
    if (!code) return;
    const product = this.products.find(item => (item.barcode || '').toLowerCase() === code || (item.sku || '').toLowerCase() === code);
    if (!product?.id) {
      this.stockWarning = `No product found for barcode ${this.barcodeScan.trim()}.`;
      return;
    }
    if (!this.isProductAvailable(product)) {
      this.stockWarning = `${product.productName} is out of stock in the selected warehouse.`;
      return;
    }
    const existingIndex = this.items.controls.findIndex(row => Number(row.get('productId')?.value) === product.id);
    const targetIndex = existingIndex >= 0 ? existingIndex : this.findEmptyItemIndex();
    if (targetIndex >= 0) {
      const row = this.items.at(targetIndex);
      row.patchValue({
        productId: product.id,
        quantity: existingIndex >= 0 ? Number(row.get('quantity')?.value || 0) + 1 : 1,
        unitPrice: Number(product.salePrice || 0),
        discount: 0,
        tax: 0
      });
      this.productSearchTerms[targetIndex] = product.productName;
    } else {
      this.addItem({ productId: product.id, quantity: 1, unitPrice: Number(product.salePrice || 0), discount: 0, tax: 0, subtotal: Number(product.salePrice || 0) });
    }
    this.barcodeScan = '';
    this.stockWarning = '';
  }

  async openScanner(): Promise<void> {
    this.scannerMessage = '';
    this.scannerOpen = true;
    setTimeout(() => this.startScanner());
  }

  closeScanner(): void {
    this.scannerOpen = false;
    this.stopScanner();
  }

  save(): void {
    this.errorMessage = '';
    if (this.showInlineCustomerForm) {
      this.form.get('newCustomer')?.markAllAsTouched();
    }
    if (this.form.invalid || !this.hasValidCustomer() || !this.items.length || (this.mode === 'edit' && !this.canEdit())) {
      this.form.markAllAsTouched();
      this.errorMessage = 'Complete all required fields and enter valid item quantities and prices.';
      return;
    }
    if (this.items.controls.some((_, index) => this.rowSubtotal(index) < 0) || this.netTotal < 0) {
      this.errorMessage = 'Discount cannot make an item subtotal or invoice total negative.';
      return;
    }
    const payment = normalizeSalesPayment(this.paymentInput);
    if (payment.paidAmount > this.netTotal) {
      this.errorMessage = 'Paid amount cannot exceed the invoice preview total. Final totals are confirmed by the backend.';
      return;
    }
    if (this.items.controls.some(row => {
      const product = this.products.find(item => item.id === Number(row.get('productId')?.value));
      return product ? !this.isProductAvailable(product) : false;
    })) {
      this.errorMessage = 'Remove unavailable products or choose a warehouse with stock before saving the invoice.';
      return;
    }
    this.submitting = true;
    this.invoiceService.saveInvoice(this.buildPayload()).subscribe({
      next: () => this.navigateToSalesList(),
      error: error => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Invoice could not be saved.');
        debugApiError('InvoiceFormComponent.save', error);
      }
    });
  }

  workflow(action: 'submit' | 'approve' | 'post'): void {
    if (!this.invoice?.id) return;
    const request = action === 'submit' ? this.invoiceService.submitInvoice(this.invoice.id)
      : action === 'approve' ? this.invoiceService.approveInvoice(this.invoice.id)
      : this.invoiceService.postInvoice(this.invoice.id);
    this.submitting = true;
    request.subscribe({
      next: saved => { this.submitting = false; this.applyInvoice(saved); },
      error: error => { this.submitting = false; this.errorMessage = extractApiErrorMessage(error, `Invoice ${action} failed.`); }
    });
  }

  cancelInvoice(): void {
    if (!this.canCancel() || !this.invoice?.id) return;
    if (!window.confirm(`Cancel invoice ${this.invoice.invoiceNo || '#' + this.invoice.id}? This action cannot be undone.`)) return;
    this.submitting = true;
    this.invoiceService.cancelInvoice(this.invoice.id).subscribe({
      next: saved => { this.submitting = false; this.applyInvoice(saved); },
      error: error => { this.submitting = false; this.errorMessage = extractApiErrorMessage(error, 'Invoice cancellation failed.'); }
    });
  }

  receivePayment(): void {
    if (this.canReceivePayment() && this.invoice?.customerId) {
      const payment = normalizeSalesPayment(this.paymentInput);
      const amount = payment.paidAmount > 0 ? payment.paidAmount : Number(this.invoice.dueAmount || 0);
      this.router.navigate(['/customers/receipts/create'], { queryParams: {
        customerId: this.invoice.customerId,
        invoiceId: this.invoice.id,
        invoiceNo: this.invoice.invoiceNo,
        dueAmount: this.invoice.dueAmount,
        customerName: this.invoice.customerName,
        amount,
        paymentMethod: toReceiptPaymentMethod(payment.paymentMethod),
        referenceNo: payment.reference,
        paymentNotes: payment.notes
      } });
    }
  }

  createReturn(): void {
    if (this.canReturn()) this.router.navigate(['/sales/returns/add'], { queryParams: { invoiceId: this.invoice?.id } });
  }

  print(): void { window.print(); }
  canEdit(): boolean { return this.invoice?.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_EDIT'); }
  canSubmit(): boolean { return this.invoice?.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_SUBMIT'); }
  canApprove(): boolean { return ['SUBMITTED', 'PENDING'].includes(this.invoice?.status || '') && this.hasPermission('SALES_INVOICE_APPROVE'); }
  canPost(): boolean { return this.invoice?.status === 'APPROVED' && this.hasPermission('SALES_INVOICE_POST'); }
  canCancel(): boolean { return ['DRAFT', 'SUBMITTED', 'APPROVED'].includes(this.invoice?.status || '') && this.hasPermission('SALES_INVOICE_CANCEL'); }
  canReceivePayment(): boolean { return this.isCompleted() && Number(this.invoice?.dueAmount || 0) > 0 && this.hasPermission('CUSTOMER_RECEIPT_CREATE'); }
  canReturn(): boolean { return this.isCompleted() && this.hasPermission('SALES_RETURN_CREATE'); }
  hasPermission(permission: string): boolean { return this.authService.hasPermission(permission); }

  private loadInvoice(id: number): void {
    this.loading = true;
    this.invoiceService.getInvoiceById(id).subscribe({
      next: invoice => {
        this.applyInvoice(invoice);
        this.loading = false;
        if (this.mode === 'details' && this.route.snapshot.queryParamMap.get('print') === 'true') setTimeout(() => window.print());
      },
      error: error => { this.loading = false; this.errorMessage = extractApiErrorMessage(error, 'Invoice could not be loaded.'); }
    });
  }

  private loadReferenceData(): void {
    this.orderService.getAllOrders().subscribe({ next: orders => this.orders = orders.filter(order => order.status === 'APPROVED'), error: error => debugApiError('InvoiceForm.orders', error) });
    this.customerService.getAllCustomers().subscribe({ next: customers => {
      this.customers = customers;
      if (this.mode === 'create') {
        const walkIn = customers.find(customer => /walk[\s-]*in/i.test(customer.name || ''));
        if (walkIn) this.selectCustomer(walkIn);
      }
    }, error: error => debugApiError('InvoiceForm.customers', error) });
    this.productService.getAllProducts().subscribe({ next: products => this.products = products, error: error => debugApiError('InvoiceForm.products', error) });
    this.warehouseService.getAllWarehouses().subscribe({ next: warehouses => {
      this.warehouses = warehouses.filter(warehouse => warehouse.active !== false);
      if (this.mode === 'create') {
        const savedId = Number(localStorage.getItem(this.warehouseStorageKey()) || 0);
        const saved = this.warehouses.find(warehouse => warehouse.id === savedId);
        const warehouse = saved || (this.warehouses.length === 1 ? this.warehouses[0] : undefined);
        if (warehouse?.id) this.form.patchValue({ warehouseId: warehouse.id });
      }
    }, error: error => debugApiError('InvoiceForm.warehouses', error) });
    if (this.hasPermission('INVENTORY_VIEW')) {
      this.stockService.getAllStock().subscribe({
        next: stocks => { this.stocks = stocks; this.stockAvailable = true; },
        error: error => debugApiError('InvoiceForm.stock', error)
      });
    }
  }

  private applyInvoice(invoice: SalesInvoice): void {
    this.invoice = invoice;
    this.clearItems();
    this.form.patchValue({ orderId: invoice.orderId ?? null, customerId: invoice.customerId, warehouseId: invoice.warehouseId, saleDate: (invoice.saleDate || '').slice(0, 10), notes: invoice.notes || '', discountType: 'FIXED', discountValue: invoice.discountAmount || 0, vatPercentage: 0, adjustmentAmount: 0 });
    this.customerSearchTerm = invoice.customerName || this.customers.find(customer => customer.id === invoice.customerId)?.name || '';
    (invoice.items || []).forEach(item => this.addItem(item));
    if (!this.items.length) this.addItem();
    if (this.mode === 'details') {
      this.form.disable({ emitEvent: false });
      if (this.canReceivePayment()) this.form.get('payment')?.enable({ emitEvent: false });
    }
  }

  private buildPayload(): SalesInvoice {
    const value = this.form.getRawValue();
    return {
      id: this.mode === 'edit' ? this.invoice?.id : undefined,
      orderId: value.orderId ? Number(value.orderId) : null,
      customerId: value.customerId ? Number(value.customerId) : null,
      newCustomer: this.showInlineCustomerForm ? this.normalizeInlineCustomer(value.newCustomer) : null,
      warehouseId: Number(value.warehouseId), saleDate: value.saleDate,
      notes: value.notes || '', totalAmount: this.totalAmount, discountAmount: this.discountAmount,
      taxAmount: this.taxAmount, netTotal: this.netTotal, paidAmount: Number(this.invoice?.paidAmount || 0),
      dueAmount: this.mode === 'edit' ? Number(this.invoice?.dueAmount || 0) : 0, paymentStatus: this.paymentStatus,
      status: this.mode === 'edit' ? this.invoice?.status : 'DRAFT',
      items: this.buildInvoiceItems(value.items)
    };
  }

  private clearItems(): void {
    while (this.items.length) this.items.removeAt(0);
    this.productSearchTerms = [];
    this.productMenusOpen = [];
  }
  private today(): string { return new Date().toISOString().slice(0, 10); }
  private isCompleted(): boolean { return ['POSTED', 'CLOSED', 'PARTIAL_PAID', 'PAID', 'COMPLETED'].includes(this.invoice?.status || ''); }

  onPaymentMethodChange(): void {
    if (this.form.get('payment.paymentMethod')?.value === 'CREDIT') {
      this.form.get('payment.paidAmount')?.setValue(0);
    }
  }

  private get paymentInput(): SalesPaymentInput {
    const payment = (this.form.get('payment') as FormGroup)?.getRawValue() || {};
    return {
      paidAmount: Number(payment.paidAmount || 0),
      paymentMethod: (payment.paymentMethod || 'CASH') as SalesPaymentMethod,
      reference: payment.reference,
      notes: payment.notes
    };
  }

  private warnForStock(product: Product): void {
    const warehouseId = Number(this.form.get('warehouseId')?.value || 0);
    if (!this.stockAvailable || !warehouseId || product.type === 'SERVICE') return;
    const quantity = Number(this.stocks.find(stock => stock.productId === product.id && stock.warehouseId === warehouseId)?.quantity || 0);
    if (quantity <= 0) this.stockWarning = `${product.productName} is out of stock in the selected warehouse.`;
  }

  private navigateToSalesList(): void {
    this.submitting = false;
    this.router.navigate(['/sales']);
  }

  private warehouseStorageKey(): string {
    const user = this.authService.getCurrentUser() as any;
    return `sales_last_warehouse_${user?.id || user?.username || 'current'}`;
  }

  isProductAvailable(product: Product): boolean {
    if (product.type === 'SERVICE') return true;
    if (!this.form.get('warehouseId')?.value) return false;
    if (!this.stockAvailable) return true;
    return this.productStock(product) > 0;
  }

  private productStock(product: Product): number {
    const warehouseId = Number(this.form.get('warehouseId')?.value || 0);
    return Number(this.stocks.find(stock => stock.productId === product.id && stock.warehouseId === warehouseId)?.quantity || 0);
  }

  hasValidCustomer(): boolean {
    const name = String(this.form.get('newCustomer.name')?.value || '').trim();
    return Boolean(this.form.get('customerId')?.value) || (this.showInlineCustomerForm && name.length > 0 && Boolean(this.form.get('newCustomer')?.valid));
  }

  private normalizeInlineCustomer(value: any): Partial<SalesCustomer> {
    return {
      name: (value?.name || '').trim(),
      phone: (value?.phone || '').trim() || undefined,
      email: (value?.email || '').trim() || undefined,
      address: (value?.address || '').trim() || undefined
    };
  }

  private buildInvoiceItems(rawItems: any[]): SalesInvoiceLineItem[] {
    const positiveAdjustment = Math.max(this.adjustmentAmount, 0);
    const negativeAdjustment = Math.abs(Math.min(this.adjustmentAmount, 0));
    const totalDiscount = this.discountAmount + negativeAdjustment;
    const totalTax = this.taxAmount + positiveAdjustment;
    let allocatedDiscount = 0;
    let allocatedTax = 0;
    return rawItems.map((item: any, index: number) => {
      const gross = Number(item.quantity) * Number(item.unitPrice);
      const rowDiscount = Number(item.discount || 0);
      const rowTax = Number(item.tax || 0);
      const isLast = index === rawItems.length - 1;
      const ratio = this.grossAmount > 0 ? gross / this.grossAmount : 0;
      const invoiceDiscount = isLast ? this.roundMoney(totalDiscount - allocatedDiscount) : this.roundMoney(totalDiscount * ratio);
      const invoiceTax = isLast ? this.roundMoney(totalTax - allocatedTax) : this.roundMoney(totalTax * ratio);
      allocatedDiscount += invoiceDiscount;
      allocatedTax += invoiceTax;
      const discount = this.roundMoney(rowDiscount + invoiceDiscount);
      const tax = this.roundMoney(rowTax + invoiceTax);
      return {
        productId: Number(item.productId),
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice),
        discount,
        tax,
        subtotal: this.roundMoney(gross - discount + tax)
      };
    });
  }

  private findEmptyItemIndex(): number {
    return this.items.controls.findIndex(row => !row.get('productId')?.value);
  }

  private roundMoney(value: number): number {
    return Math.round((Number(value) || 0) * 100) / 100;
  }

  private availableProducts(): Product[] {
    return this.products.filter(product => this.isProductAvailable(product));
  }

  private async startScanner(): Promise<void> {
    const video = this.scannerVideo?.nativeElement;
    if (!this.scannerOpen || !video) return;
    try {
      if (!navigator.mediaDevices?.getUserMedia) {
        this.scannerMessage = 'Camera scanning is not available in this browser.';
        return;
      }
      this.stopScanner();
      this.scannerReader = this.scannerReader || new BrowserMultiFormatReader();
      this.scannerControls = await this.scannerReader.decodeFromVideoDevice(undefined, video, (result: Result | undefined) => {
        const text = result?.getText()?.trim();
        if (!text) return;
        this.barcodeScan = text;
        this.closeScanner();
        this.scanBarcode();
      });
    } catch (error: any) {
      this.scannerMessage = this.scannerErrorMessage(error);
      debugApiError('InvoiceForm.startScanner', error);
    }
  }

  private stopScanner(): void {
    this.scannerControls?.stop();
    this.scannerControls = undefined;
    const video = this.scannerVideo?.nativeElement;
    if (video) video.srcObject = null;
  }

  private scannerErrorMessage(error: any): string {
    const name = error?.name || '';
    if (name === 'NotAllowedError' || name === 'PermissionDeniedError') {
      return 'Camera permission was denied. Allow camera access and try again.';
    }
    if (name === 'NotFoundError' || name === 'DevicesNotFoundError') {
      return 'No camera was found on this device.';
    }
    if (name === 'NotReadableError' || name === 'TrackStartError') {
      return 'The camera is already in use by another application.';
    }
    return 'Unable to start the camera scanner. Check camera permission and try again.';
  }
}
