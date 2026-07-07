import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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

@Component({
  selector: 'app-sales-invoice-form',
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.css']
})
export class InvoiceFormComponent implements OnInit {
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
      customerId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      saleDate: [this.today(), Validators.required],
      notes: ['', Validators.maxLength(500)],
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

  get items(): FormArray { return this.form.get('items') as FormArray; }
  get totalAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('quantity')?.value || 0) * Number(row.get('unitPrice')?.value || 0), 0); }
  get discountAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('discount')?.value || 0), 0); }
  get taxAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('tax')?.value || 0), 0); }
  get netTotal(): number { return this.totalAmount - this.discountAmount + this.taxAmount; }
  get dueAmount(): number | null { return this.invoice ? Number(this.invoice.dueAmount || 0) : null; }
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
  }

  removeItem(index: number): void {
    if (this.items.length > 1) this.items.removeAt(index);
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

  onCustomerInput(value: string): void {
    this.customerSearchTerm = value;
    this.customerMenuOpen = true;
    const term = value.trim().toLowerCase();
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
  }

  createCustomer(): void {
    const url = this.router.serializeUrl(this.router.createUrlTree(['/customers/create']));
    window.open(url, '_blank', 'noopener');
    this.customerMenuOpen = false;
  }

  onWarehouseChange(): void {
    const warehouseId = Number(this.form.get('warehouseId')?.value || 0);
    if (warehouseId) localStorage.setItem(this.warehouseStorageKey(), String(warehouseId));
    this.stockWarning = '';
    this.items.controls.forEach(row => {
      const product = this.products.find(item => item.id === Number(row.get('productId')?.value));
      if (product) this.warnForStock(product);
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

  save(confirmInvoice = false): void {
    this.errorMessage = '';
    if (this.form.invalid || !this.items.length || (this.mode === 'edit' && !this.canEdit())) {
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
    this.submitting = true;
    this.invoiceService.saveInvoice(this.buildPayload()).subscribe({
      next: saved => {
        if (confirmInvoice && saved.id && this.hasPermission('SALES_INVOICE_SUBMIT')) {
          this.invoiceService.submitInvoice(saved.id).subscribe({
            next: confirmed => this.navigateToSavedInvoice(confirmed, payment),
            error: error => {
              this.submitting = false;
              this.errorMessage = extractApiErrorMessage(error, 'Draft was saved, but invoice confirmation failed.');
              this.navigateToSavedInvoice(saved, payment);
            }
          });
          return;
        }
        this.navigateToSavedInvoice(saved, payment);
      },
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
    this.form.patchValue({ orderId: invoice.orderId ?? null, customerId: invoice.customerId, warehouseId: invoice.warehouseId, saleDate: (invoice.saleDate || '').slice(0, 10), notes: invoice.notes || '' });
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
      customerId: Number(value.customerId), warehouseId: Number(value.warehouseId), saleDate: value.saleDate,
      notes: value.notes || '', totalAmount: this.totalAmount, discountAmount: this.discountAmount,
      taxAmount: this.taxAmount, netTotal: this.netTotal, paidAmount: Number(this.invoice?.paidAmount || 0),
      dueAmount: this.mode === 'edit' ? Number(this.invoice?.dueAmount || 0) : 0, paymentStatus: this.paymentStatus,
      status: this.mode === 'edit' ? this.invoice?.status : 'DRAFT',
      items: value.items.map((item: any, index: number) => ({
        productId: Number(item.productId), quantity: Number(item.quantity), unitPrice: Number(item.unitPrice),
        discount: Number(item.discount), tax: Number(item.tax), subtotal: this.rowSubtotal(index)
      }))
    };
  }

  private clearItems(): void { while (this.items.length) this.items.removeAt(0); }
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
    if (quantity <= 0) this.stockWarning = `${product.productName} has no available stock in the selected warehouse. You may continue or change the product/warehouse.`;
  }

  private navigateToSavedInvoice(saved: SalesInvoice, payment: SalesPaymentInput): void {
    this.submitting = false;
    this.router.navigate(['/sales', saved.id, 'view'], { queryParams: payment.paidAmount > 0 ? {
      paymentAmount: payment.paidAmount,
      paymentMethod: payment.paymentMethod,
      paymentReference: payment.reference,
      paymentNotes: payment.notes
    } : undefined });
  }

  private warehouseStorageKey(): string {
    const user = this.authService.getCurrentUser() as any;
    return `sales_last_warehouse_${user?.id || user?.username || 'current'}`;
  }
}
