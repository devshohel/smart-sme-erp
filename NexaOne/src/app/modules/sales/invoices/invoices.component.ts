import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { Product } from '../../../models/product.model';
import { PaymentStatus, SalesCustomer, SalesInvoiceLineItem, SalesInvoiceStatus } from '../../../models/sales-common.model';
import { SalesInvoice } from '../../../models/sales-invoice.model';
import { SalesOrder } from '../../../models/sales-order.model';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { SalesOrderService } from '../../../services/sales-order.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-invoices',
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {
  invoiceForm: FormGroup;
  invoices: SalesInvoice[] = [];
  orders: SalesOrder[] = [];
  customers: SalesCustomer[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  selectedInvoice: SalesInvoice | null = null;
  editingInvoiceId: number | null = null;
  currentMode: 'list' | 'create' | 'edit' | 'details' = 'list';
  searchTerm = '';
  statusFilter = '';
  dateFilter = '';
  customerSearchTerm = '';

  readonly paymentStatuses: PaymentStatus[] = ['PAID', 'PARTIAL', 'DUE'];
  readonly statuses: SalesInvoiceStatus[] = ['DRAFT', 'SUBMITTED', 'APPROVED', 'POSTED', 'PARTIAL_PAID', 'PAID', 'CANCELLED'];

  constructor(
    private fb: FormBuilder,
    private invoiceService: SalesInvoiceService,
    private orderService: SalesOrderService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.invoiceForm = this.fb.group({
      orderId: [null],
      customerId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      saleDate: [this.today(), Validators.required],
      paidAmount: [{ value: 0, disabled: true }],
      notes: ['', [Validators.maxLength(500)]],
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.loadInvoices();
    this.loadReferenceData();
    this.route.data.subscribe(data => {
      this.currentMode = data['mode'] || 'list';
      if (this.currentMode === 'create') {
        this.resetForm();
      }
    });
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id') || 0);
      if (id > 0) {
        this.loadInvoices(id);
      }
    });
  }

  get items(): FormArray {
    return this.invoiceForm.get('items') as FormArray;
  }

  get totalAmount(): number {
    return this.items.controls.reduce((sum, control) => {
      const quantity = Number(control.get('quantity')?.value || 0);
      const unitPrice = Number(control.get('unitPrice')?.value || 0);
      return sum + (quantity * unitPrice);
    }, 0);
  }

  get discountAmount(): number {
    return this.items.controls.reduce((sum, control) => sum + Number(control.get('discount')?.value || 0), 0);
  }

  get taxAmount(): number {
    return this.items.controls.reduce((sum, control) => sum + Number(control.get('tax')?.value || 0), 0);
  }

  get netTotal(): number {
    return this.totalAmount - this.discountAmount + this.taxAmount;
  }

  get dueAmount(): number {
    return Math.max(this.netTotal - Number(this.invoiceForm.get('paidAmount')?.value || 0), 0);
  }

  get paymentStatus(): PaymentStatus {
    const paidAmount = Number(this.invoiceForm.get('paidAmount')?.value || 0);
    if (this.netTotal <= 0) {
      return 'DUE';
    }
    if (paidAmount >= this.netTotal) {
      return 'PAID';
    }
    if (paidAmount > 0) {
      return 'PARTIAL';
    }
    return 'DUE';
  }

  get filteredCustomers(): SalesCustomer[] {
    const keyword = this.customerSearchTerm.trim().toLowerCase();
    if (!keyword) {
      return this.customers.slice(0, 8);
    }
    return this.customers.filter(customer =>
      (customer.name || '').toLowerCase().includes(keyword)
      || (customer.phone || '').toLowerCase().includes(keyword)
      || ((customer.customerCode || '') as string).toLowerCase().includes(keyword)
    ).slice(0, 8);
  }

  loadInvoices(selectedId?: number): void {
    this.loading = true;
    this.invoiceService.getAllInvoices().subscribe({
      next: (invoices) => {
        this.invoices = invoices;
        this.selectedInvoice = invoices.find(invoice => invoice.id === selectedId) || invoices[0] || null;
        if (selectedId && this.currentMode === 'edit' && this.selectedInvoice) {
          this.editInvoice(this.selectedInvoice);
        }
        if (selectedId && this.currentMode === 'details' && this.selectedInvoice) {
          this.openInvoiceDetails(this.selectedInvoice, false);
        }
        this.loading = false;
      },
      error: (error) => {
        this.invoices = [];
        this.selectedInvoice = null;
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sales invoices could not be loaded.');
        debugApiError('InvoicesComponent.loadInvoices', error);
      }
    });
  }

  loadReferenceData(): void {
    this.orderService.getAllOrders().subscribe({
      next: orders => this.orders = orders.filter(order => order.status === 'APPROVED'),
      error: error => debugApiError('InvoicesComponent.loadOrders', error)
    });

    this.customerService.getAllCustomers().subscribe({
      next: customers => this.customers = customers,
      error: error => debugApiError('InvoicesComponent.loadCustomers', error)
    });

    this.productService.getAllProducts().subscribe({
      next: products => this.products = products,
      error: (error) => {
        this.products = [];
        debugApiError('InvoicesComponent.loadProducts', error);
      }
    });

    this.warehouseService.getAllWarehouses().subscribe({
      next: warehouses => this.warehouses = warehouses,
      error: (error) => {
        this.warehouses = [];
        debugApiError('InvoicesComponent.loadWarehouses', error);
      }
    });
  }

  addItem(item?: Partial<SalesInvoiceLineItem>): void {
    const group = this.fb.group({
      productId: [item?.productId ?? null, Validators.required],
      quantity: [item?.quantity ?? 1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [item?.unitPrice ?? 0, [Validators.required, Validators.min(0)]],
      discount: [item?.discount ?? 0, [Validators.required, Validators.min(0)]],
      tax: [item?.tax ?? 0, [Validators.required, Validators.min(0)]],
      subtotal: [{ value: item?.subtotal ?? 0, disabled: true }]
    });

    ['quantity', 'unitPrice', 'discount', 'tax'].forEach(field => {
      group.get(field)?.valueChanges.subscribe(() => this.recalculateRow(group));
    });

    this.items.push(group);
    this.recalculateRow(group);
  }

  removeItem(index: number): void {
    if (this.items.length === 1) {
      return;
    }

    this.items.removeAt(index);
  }

  onProductChange(index: number): void {
    const group = this.items.at(index) as FormGroup;
    const product = this.products.find(item => item.id === Number(group.get('productId')?.value));
    if (product) {
      group.patchValue({ unitPrice: product.salePrice || 0 }, { emitEvent: true });
    }
  }

  recalculateRow(group: FormGroup): void {
    const quantity = Number(group.get('quantity')?.value || 0);
    const unitPrice = Number(group.get('unitPrice')?.value || 0);
    const discount = Number(group.get('discount')?.value || 0);
    const tax = Number(group.get('tax')?.value || 0);
    const subtotal = (quantity * unitPrice) - discount + tax;
    group.get('subtotal')?.setValue(subtotal, { emitEvent: false });
  }

  onOrderChange(): void {
    const orderId = Number(this.invoiceForm.get('orderId')?.value || 0);
    const order = this.orders.find(item => item.id === orderId);
    if (!order) {
      return;
    }

    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.invoiceForm.patchValue({
      customerId: order.customerId,
      warehouseId: order.warehouseId
    });
    this.customerSearchTerm = order.customerName || '';

    if (!order.items.length) {
      this.addItem();
      return;
    }

    order.items.forEach(item => this.addItem({
      productId: item.productId,
      uomId: item.uomId,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      discount: 0,
      tax: 0,
      subtotal: item.subtotal
    }));
  }

  saveInvoice(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.invoiceForm.invalid || this.items.length === 0) {
      this.invoiceForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const payload = this.buildInvoicePayload();

    this.invoiceService.saveInvoice(payload).subscribe({
      next: (saved) => {
        this.submitting = false;
        this.successMessage = this.editingInvoiceId ? 'Invoice updated successfully.' : 'Invoice saved successfully.';
        this.selectedInvoice = saved;
        this.resetForm();
        this.loadInvoices(saved.id);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Invoice could not be saved.');
        debugApiError('InvoicesComponent.saveInvoice', error);
      }
    });
  }

  selectInvoice(invoice: SalesInvoice): void {
    this.selectedInvoice = invoice;
  }

  viewInvoice(invoice: SalesInvoice): void {
    this.openInvoiceDetails(invoice, true);
  }

  editInvoice(invoice: SalesInvoice): void {
    this.invoiceForm.enable({ emitEvent: false });
    this.currentMode = 'edit';
    this.editingInvoiceId = invoice.id || null;
    this.selectedInvoice = invoice;
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }
    this.invoiceForm.patchValue({
      orderId: invoice.orderId ?? null,
      customerId: invoice.customerId,
      warehouseId: invoice.warehouseId,
      saleDate: this.toDateInput(invoice.saleDate),
      paidAmount: Number(invoice.paidAmount || 0),
      notes: invoice.notes || ''
    });
    this.customerSearchTerm = invoice.customerName || '';
    (invoice.items || []).forEach(item => this.addItem(item));
  }

  get filteredInvoices(): SalesInvoice[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.invoices.filter(invoice => {
      const matchesKeyword = !keyword
        || (invoice.invoiceNo || '').toLowerCase().includes(keyword)
        || (invoice.customerName || '').toLowerCase().includes(keyword)
        || (invoice.warehouseName || '').toLowerCase().includes(keyword);
      const matchesStatus = !this.statusFilter || invoice.status === this.statusFilter;
      const matchesDate = !this.dateFilter || (invoice.saleDate || '').slice(0, 10) === this.dateFilter;
      return matchesKeyword && matchesStatus && matchesDate;
    }).sort((left, right) => this.sortNewestFirst(left.id, right.id, left['createdAt'], right['createdAt']));
  }

  resetForm(): void {
    this.invoiceForm.enable({ emitEvent: false });
    this.editingInvoiceId = null;
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.invoiceForm.reset({
      customerId: null,
      orderId: null,
      warehouseId: null,
      saleDate: this.today(),
      paidAmount: 0,
      notes: ''
    });
    this.customerSearchTerm = '';
    this.addItem();
    if (this.currentMode !== 'create') {
      this.router.navigate(['/sales/invoices']);
    }
  }

  selectCustomer(customer: SalesCustomer): void {
    this.invoiceForm.patchValue({ customerId: customer.id });
    this.customerSearchTerm = customer.name || '';
  }

  overrideInvoiceSubTotal(value: string): void {
    const target = this.parseSummaryValue(value);
    if (target === null) {
      return;
    }
    const lastItem = this.lastItemGroup();
    if (!lastItem) {
      return;
    }
    const quantity = Number(lastItem.get('quantity')?.value || 0);
    if (quantity <= 0) {
      return;
    }
    const diff = target - this.totalAmount;
    const currentGross = Number(lastItem.get('quantity')?.value || 0) * Number(lastItem.get('unitPrice')?.value || 0);
    const nextGross = Math.max(currentGross + diff, 0);
    lastItem.patchValue({ unitPrice: nextGross / quantity });
  }

  overrideInvoiceDiscount(value: string): void {
    this.applyDiscountTarget(this.parseSummaryValue(value));
  }

  overrideInvoiceTax(value: string): void {
    const target = this.parseSummaryValue(value);
    if (target === null) {
      return;
    }
    const lastItem = this.lastItemGroup();
    if (!lastItem) {
      return;
    }
    const nextTax = Math.max(Number(lastItem.get('tax')?.value || 0) + (target - this.taxAmount), 0);
    lastItem.patchValue({ tax: nextTax });
  }

  overrideInvoiceGrandTotal(value: string): void {
    const target = this.parseSummaryValue(value);
    if (target === null) {
      return;
    }
    const diff = target - this.netTotal;
    if (diff === 0) {
      return;
    }
    if (diff > 0) {
      const discountShift = Math.min(this.lastItemDiscount(), diff);
      if (discountShift > 0) {
        this.applyDiscountTarget(this.discountAmount - discountShift);
      }
      const remaining = diff - discountShift;
      if (remaining > 0) {
        this.overrideInvoiceTax(String(this.taxAmount + remaining));
      }
      return;
    }

    const reduction = Math.abs(diff);
    const taxShift = Math.min(this.lastItemTax(), reduction);
    if (taxShift > 0) {
      this.overrideInvoiceTax(String(this.taxAmount - taxShift));
    }
    const remaining = reduction - taxShift;
    if (remaining > 0) {
      this.applyDiscountTarget(this.discountAmount + remaining);
    }
  }

  hasError(path: string, errorName: string): boolean {
    const control = this.invoiceForm.get(path);
    return !!control && control.touched && control.hasError(errorName);
  }

  private buildInvoicePayload(): SalesInvoice {
    const value = this.invoiceForm.getRawValue();
    const customer = this.customers.find(item => item.id === Number(value.customerId));
    const warehouse = this.warehouses.find(item => item.id === Number(value.warehouseId));
    const items: SalesInvoiceLineItem[] = value.items.map((item: any) => ({
      productId: item.productId !== null ? Number(item.productId) : null,
      productName: this.products.find(product => product.id === Number(item.productId))?.productName || 'N/A',
      quantity: Number(item.quantity || 0),
      unitPrice: Number(item.unitPrice || 0),
      discount: Number(item.discount || 0),
      tax: Number(item.tax || 0),
      subtotal: Number(item.subtotal || 0)
    }));

    return {
      id: this.editingInvoiceId || undefined,
      orderId: value.orderId !== null ? Number(value.orderId) : null,
      orderNo: this.orders.find(order => order.id === Number(value.orderId))?.orderNo || '',
      customerId: value.customerId !== null ? Number(value.customerId) : null,
      customerName: customer?.name || '',
      warehouseId: value.warehouseId !== null ? Number(value.warehouseId) : null,
      warehouseName: warehouse?.name || '',
      saleDate: value.saleDate,
      notes: value.notes || '',
      items,
      totalAmount: this.totalAmount,
      discountAmount: this.discountAmount,
      taxAmount: this.taxAmount,
      netTotal: this.netTotal,
      paidAmount: 0,
      dueAmount: this.netTotal,
      paymentStatus: 'DUE',
      status: 'DRAFT'
    };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private toDateInput(value?: string): string {
    return value ? value.slice(0, 10) : this.today();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canEdit(invoice: SalesInvoice | null): boolean {
    return !!invoice && !!invoice.id && invoice.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_EDIT');
  }

  canSubmit(invoice: SalesInvoice | null): boolean {
    return !!invoice && !!invoice.id && invoice.status === 'DRAFT' && this.hasPermission('SALES_INVOICE_SUBMIT');
  }

  canApprove(invoice: SalesInvoice | null): boolean {
    return !!invoice && !!invoice.id && ['SUBMITTED', 'PENDING'].includes(invoice.status || '') && this.hasPermission('SALES_INVOICE_APPROVE');
  }

  canPost(invoice: SalesInvoice | null): boolean {
    return !!invoice && !!invoice.id && invoice.status === 'APPROVED' && this.hasPermission('SALES_INVOICE_POST');
  }

  canCancel(invoice: SalesInvoice | null): boolean {
    return !!invoice && !!invoice.id && ['DRAFT', 'SUBMITTED', 'APPROVED'].includes(invoice.status || '') && this.hasPermission('SALES_INVOICE_CANCEL');
  }

  submitSelected(): void {
    if (!this.selectedInvoice?.id) {
      return;
    }
    this.submitting = true;
    this.invoiceService.submitInvoice(this.selectedInvoice.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales invoice submitted successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Invoice submit failed.')
    });
  }

  approveSelected(): void {
    if (!this.selectedInvoice?.id) {
      return;
    }
    this.submitting = true;
    this.invoiceService.approveInvoice(this.selectedInvoice.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales invoice approved successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Invoice approval failed.')
    });
  }

  postSelected(): void {
    if (!this.selectedInvoice?.id) {
      return;
    }
    this.submitting = true;
    this.invoiceService.postInvoice(this.selectedInvoice.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales invoice posted successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Invoice posting failed.')
    });
  }

  cancelSelected(): void {
    if (!this.selectedInvoice?.id) {
      return;
    }
    this.submitting = true;
    this.invoiceService.cancelInvoice(this.selectedInvoice.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales invoice cancelled successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Invoice cancellation failed.')
    });
  }

  newReturnFromInvoice(): void {
    if (!this.selectedInvoice?.id || !this.hasPermission('SALES_RETURN_CREATE')) {
      return;
    }
    this.router.navigate(['/sales/returns/create']);
  }

  printSelected(): void {
    window.print();
  }

  private handleWorkflowSuccess(saved: SalesInvoice, message: string): void {
    this.submitting = false;
    this.successMessage = message;
    this.selectedInvoice = saved;
    this.loadInvoices(saved.id);
  }

  private handleWorkflowError(error: unknown, fallbackMessage: string): void {
    this.submitting = false;
    this.errorMessage = extractApiErrorMessage(error, fallbackMessage);
    debugApiError('InvoicesComponent.workflow', error);
  }

  private applyDiscountTarget(target: number | null): void {
    if (target === null) {
      return;
    }
    const lastItem = this.lastItemGroup();
    if (!lastItem) {
      return;
    }
    const nextDiscount = Math.max(Number(lastItem.get('discount')?.value || 0) + (target - this.discountAmount), 0);
    lastItem.patchValue({ discount: nextDiscount });
  }

  private lastItemGroup(): FormGroup | null {
    return this.items.length ? this.items.at(this.items.length - 1) as FormGroup : null;
  }

  private lastItemDiscount(): number {
    return Number(this.lastItemGroup()?.get('discount')?.value || 0);
  }

  private lastItemTax(): number {
    return Number(this.lastItemGroup()?.get('tax')?.value || 0);
  }

  private parseSummaryValue(value: string): number | null {
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed >= 0 ? parsed : null;
  }

  private sortNewestFirst(leftId?: number, rightId?: number, leftCreatedAt?: string, rightCreatedAt?: string): number {
    const leftTime = leftCreatedAt ? new Date(leftCreatedAt).getTime() : 0;
    const rightTime = rightCreatedAt ? new Date(rightCreatedAt).getTime() : 0;
    if (leftTime !== rightTime) {
      return rightTime - leftTime;
    }
    return (rightId || 0) - (leftId || 0);
  }

  private openInvoiceDetails(invoice: SalesInvoice, navigate: boolean): void {
    this.editInvoice(invoice);
    this.currentMode = 'details';
    this.invoiceForm.disable({ emitEvent: false });
    if (navigate && invoice.id) {
      this.router.navigate(['/sales/invoices/details', invoice.id]);
    }
  }
}
