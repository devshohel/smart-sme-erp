import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../models/product.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { SalesCustomer, SalesOrderLineItem, SalesOrderStatus } from '../../../models/sales-common.model';
import { SalesOrder } from '../../../models/sales-order.model';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { SalesOrderService } from '../../../services/sales-order.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';
import { SalesFeatureSettings } from '../../auth/auth.model';
import { SettingsService } from '../../settings/settings.service';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orderForm: FormGroup;
  orders: SalesOrder[] = [];
  customers: SalesCustomer[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  selectedOrder: SalesOrder | null = null;
  editingOrderId: number | null = null;
  currentMode: 'list' | 'approval' | 'create' | 'edit' | 'details' = 'list';
  searchTerm = '';
  statusFilter = '';
  dateFrom = '';
  dateTo = '';
  customerSearchTerm = '';
  currentPage = 1;
  readonly pageSize = 10;
  salesFeatures: SalesFeatureSettings = {
    enableControlledSalesMode: false,
    enableSalesOrders: false,
    enableQuotations: false,
    enableDeliveryNotes: false,
    enableSalesApproval: false,
    enableManualAllocation: false,
    enableAdvancedInvoice: false
  };

  readonly statuses: SalesOrderStatus[] = ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CONVERTED', 'CANCELLED'];

  constructor(
    private fb: FormBuilder,
    private orderService: SalesOrderService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private authService: AuthService,
    private settingsService: SettingsService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.orderForm = this.fb.group({
      customerId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      orderDate: [this.today(), Validators.required],
      status: ['DRAFT', Validators.required],
      notes: ['', [Validators.maxLength(500)]],
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.settingsService.salesFeatures$.subscribe(settings => this.salesFeatures = settings);
    this.settingsService.loadSalesFeatures().subscribe({ error: () => undefined });
    this.loadReferenceData();
    this.route.data.subscribe(data => {
      this.currentMode = data['mode'] || 'list';
      if (this.currentMode === 'create') {
        this.resetForm();
      }
    });
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id') || 0);
      this.loadOrders(id || undefined);
    });
  }

  get items(): FormArray {
    return this.orderForm.get('items') as FormArray;
  }

  loadOrders(selectedId?: number): void {
    this.loading = true;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.selectedOrder = orders.find(order => order.id === selectedId) || orders[0] || null;
        if (selectedId && this.currentMode === 'edit' && this.selectedOrder) {
          this.editOrder(this.selectedOrder);
        }
        if (selectedId && this.currentMode === 'details' && this.selectedOrder) {
          this.openOrderDetails(this.selectedOrder, false);
        }
        this.loading = false;
      },
      error: (error) => {
        this.orders = [];
        this.selectedOrder = null;
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sales orders could not be loaded.');
        debugApiError('OrdersComponent.loadOrders', error);
      }
    });
  }

  loadReferenceData(): void {
    this.customerService.getAllCustomers().subscribe({
      next: customers => this.customers = customers,
      error: error => debugApiError('OrdersComponent.loadCustomers', error)
    });

    this.productService.getAllProducts().subscribe({
      next: products => this.products = products,
      error: (error) => {
        this.products = [];
        debugApiError('OrdersComponent.loadProducts', error);
      }
    });

    this.warehouseService.getAllWarehouses().subscribe({
      next: warehouses => this.warehouses = warehouses,
      error: (error) => {
        this.warehouses = [];
        debugApiError('OrdersComponent.loadWarehouses', error);
      }
    });
  }

  addItem(item?: Partial<SalesOrderLineItem>): void {
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

  get subTotalAmount(): number {
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

  get grandTotal(): number {
    return this.subTotalAmount - this.discountAmount + this.taxAmount;
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

  get filteredOrders(): SalesOrder[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.orders.filter(order => {
      const matchesKeyword = !keyword
        || (order.orderNo || '').toLowerCase().includes(keyword)
        || (order.customerName || '').toLowerCase().includes(keyword)
        || (order.warehouseName || '').toLowerCase().includes(keyword);
      const matchesStatus = !this.statusFilter || order.status === this.statusFilter;
      const orderDate = (order.orderDate || '').slice(0, 10);
      const matchesDate = (!this.dateFrom || orderDate >= this.dateFrom) && (!this.dateTo || orderDate <= this.dateTo);
      const matchesQueue = this.currentMode !== 'approval' || ['SUBMITTED', 'PENDING'].includes(order.status);
      return matchesKeyword && matchesStatus && matchesDate && matchesQueue;
    }).sort((left, right) => this.sortNewestFirst(left.id, right.id, left['createdAt'], right['createdAt']));
  }

  get totalPages(): number { return Math.max(1, Math.ceil(this.filteredOrders.length / this.pageSize)); }
  get paginatedOrders(): SalesOrder[] {
    const page = Math.min(this.currentPage, this.totalPages);
    return this.filteredOrders.slice((page - 1) * this.pageSize, page * this.pageSize);
  }
  get pageStart(): number { return this.filteredOrders.length ? (Math.min(this.currentPage, this.totalPages) - 1) * this.pageSize + 1 : 0; }
  get pageEnd(): number { return Math.min(this.pageStart + this.pageSize - 1, this.filteredOrders.length); }
  filtersChanged(): void { this.currentPage = 1; }
  goToPage(page: number): void { this.currentPage = Math.min(Math.max(page, 1), this.totalPages); }

  saveOrder(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.orderForm.invalid || this.items.length === 0) {
      this.orderForm.markAllAsTouched();
      this.errorMessage = 'Complete all required fields and enter valid item quantities and prices.';
      return;
    }

    this.submitting = true;
    const payload = this.buildOrderPayload();

    this.orderService.saveOrder(payload).subscribe({
      next: (saved) => {
        this.submitting = false;
        this.successMessage = this.editingOrderId ? 'Sales order updated successfully.' : 'Sales order saved successfully.';
        this.selectedOrder = saved;
        this.resetForm();
        this.loadOrders(saved.id);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sales order could not be saved.');
        debugApiError('OrdersComponent.saveOrder', error);
      }
    });
  }

  selectOrder(order: SalesOrder): void {
    this.selectedOrder = order;
    if (this.currentMode === 'details' && order.id) {
      this.router.navigate(['/sales/orders/details', order.id]);
    }
  }

  viewOrder(order: SalesOrder): void {
    this.openOrderDetails(order, true);
  }

  editOrder(order: SalesOrder): void {
    this.orderForm.enable({ emitEvent: false });
    this.currentMode = 'edit';
    this.editingOrderId = order.id || null;
    this.selectedOrder = order;
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.orderForm.patchValue({
      customerId: order.customerId,
      warehouseId: order.warehouseId,
      orderDate: this.toDateInput(order.orderDate),
      status: order.status || 'DRAFT',
      notes: order.notes || ''
    });
    this.customerSearchTerm = order.customerName || '';

    if (!order.items.length) {
      this.addItem();
      return;
    }

    order.items.forEach(item => this.addItem(item));
  }

  resetForm(): void {
    this.orderForm.enable({ emitEvent: false });
    this.editingOrderId = null;
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.orderForm.reset({
      customerId: null,
      warehouseId: null,
      orderDate: this.today(),
      status: 'DRAFT',
      notes: ''
    });
    this.customerSearchTerm = '';
    this.addItem();
    if (this.currentMode !== 'create') {
      this.router.navigate(['/sales/orders']);
    }
  }

  selectCustomer(customer: SalesCustomer): void {
    this.orderForm.patchValue({ customerId: customer.id });
    this.customerSearchTerm = customer.name || '';
  }

  overrideSubTotal(value: string): void {
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
    const diff = target - this.subTotalAmount;
    const currentGross = Number(lastItem.get('quantity')?.value || 0) * Number(lastItem.get('unitPrice')?.value || 0);
    const nextGross = Math.max(currentGross + diff, 0);
    lastItem.patchValue({ unitPrice: nextGross / quantity });
  }

  overrideDiscountAmount(value: string): void {
    this.applyDiscountDelta(this.parseSummaryValue(value));
  }

  overrideTaxAmount(value: string): void {
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

  overrideGrandTotal(value: string): void {
    const target = this.parseSummaryValue(value);
    if (target === null) {
      return;
    }
    const diff = target - this.grandTotal;
    if (diff === 0) {
      return;
    }
    if (diff > 0) {
      const discountShift = Math.min(this.lastItemDiscount(), diff);
      if (discountShift > 0) {
        this.applyDiscountDelta(this.discountAmount - discountShift);
      }
      const remaining = diff - discountShift;
      if (remaining > 0) {
        this.overrideTaxAmount(String(this.taxAmount + remaining));
      }
      return;
    }

    const reduction = Math.abs(diff);
    const taxShift = Math.min(this.lastItemTax(), reduction);
    if (taxShift > 0) {
      this.overrideTaxAmount(String(this.taxAmount - taxShift));
    }
    const remaining = reduction - taxShift;
    if (remaining > 0) {
      this.applyDiscountDelta(this.discountAmount + remaining);
    }
  }

  productName(productId: number | null): string {
    return this.products.find(item => item.id === productId)?.productName || 'N/A';
  }

  hasError(path: string, errorName: string): boolean {
    const control = this.orderForm.get(path);
    return !!control && control.touched && control.hasError(errorName);
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canEdit(order: SalesOrder | null): boolean {
    return !!order && !!order.id && ['DRAFT', 'REJECTED'].includes(order.status) && this.hasPermission('SALES_ORDER_EDIT');
  }

  canSubmit(order: SalesOrder | null): boolean {
    return !!order && !!order.id && ['DRAFT', 'REJECTED'].includes(order.status) && this.hasPermission('SALES_ORDER_SUBMIT');
  }

  canApprove(order: SalesOrder | null): boolean {
    return this.approvalRequired() && !!order && !!order.id && ['SUBMITTED', 'PENDING'].includes(order.status) && this.hasPermission('SALES_ORDER_APPROVE');
  }

  canReject(order: SalesOrder | null): boolean {
    return this.approvalRequired() && !!order && !!order.id && ['SUBMITTED', 'PENDING'].includes(order.status) && this.hasPermission('SALES_ORDER_REJECT');
  }

  canCancel(order: SalesOrder | null): boolean {
    return !!order && !!order.id && !['CONVERTED', 'CANCELLED'].includes(order.status) && this.hasPermission('SALES_ORDER_CANCEL');
  }

  canConvert(order: SalesOrder | null): boolean {
    return !!order && !!order.id && order.status === 'APPROVED' && this.hasPermission('SALES_ORDER_CONVERT');
  }

  approvalRequired(): boolean {
    return !this.salesFeatures.enableControlledSalesMode || this.salesFeatures.enableSalesApproval;
  }

  printSelected(): void {
    window.print();
  }

  submitSelected(): void {
    if (!this.selectedOrder?.id) {
      return;
    }
    this.submitting = true;
    this.orderService.submitOrder(this.selectedOrder.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales order submitted successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Sales order submit failed.')
    });
  }

  approveSelected(): void {
    if (!this.selectedOrder?.id) {
      return;
    }
    this.submitting = true;
    this.orderService.approveOrder(this.selectedOrder.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales order approved successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Sales order approval failed.')
    });
  }

  rejectSelected(): void {
    if (!this.selectedOrder?.id) {
      return;
    }
    const reason = window.prompt('Rejection reason');
    if (!reason) {
      return;
    }
    this.submitting = true;
    this.orderService.rejectOrder(this.selectedOrder.id, reason).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales order rejected successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Sales order rejection failed.')
    });
  }

  cancelSelected(): void {
    if (!this.selectedOrder?.id) {
      return;
    }
    if (!window.confirm(`Cancel sales order ${this.selectedOrder.orderNo || ''}?`)) {
      return;
    }
    this.submitting = true;
    this.orderService.cancelOrder(this.selectedOrder.id).subscribe({
      next: (saved) => this.handleWorkflowSuccess(saved, 'Sales order cancelled successfully.'),
      error: (error) => this.handleWorkflowError(error, 'Sales order cancellation failed.')
    });
  }

  convertSelected(): void {
    if (!this.selectedOrder?.id) {
      return;
    }
    this.submitting = true;
    this.orderService.convertOrderToInvoice(this.selectedOrder.id).subscribe({
      next: invoice => {
        this.submitting = false;
        this.successMessage = 'Sales order converted to invoice successfully.';
        this.loadOrders(this.selectedOrder?.id);
        this.router.navigate(invoice.id ? ['/sales', invoice.id, 'view'] : ['/sales']);
      },
      error: (error) => this.handleWorkflowError(error, 'Order conversion failed.')
    });
  }

  private buildOrderPayload(): SalesOrder {
    const value = this.orderForm.getRawValue();
    const customer = this.customers.find(item => item.id === Number(value.customerId));
    const warehouse = this.warehouses.find(item => item.id === Number(value.warehouseId));
    const items: SalesOrderLineItem[] = value.items.map((item: any) => ({
      productId: item.productId !== null ? Number(item.productId) : null,
      productName: this.productName(item.productId !== null ? Number(item.productId) : null),
      quantity: Number(item.quantity || 0),
      unitPrice: Number(item.unitPrice || 0),
      discount: Number(item.discount || 0),
      tax: Number(item.tax || 0),
      subtotal: Number(item.subtotal || 0)
    }));

    return {
      id: this.editingOrderId || undefined,
      orderNo: this.editingOrderId ? this.selectedOrder?.orderNo : undefined,
      customerId: value.customerId !== null ? Number(value.customerId) : null,
      customerName: customer?.name || '',
      warehouseId: value.warehouseId !== null ? Number(value.warehouseId) : null,
      warehouseName: warehouse?.name || '',
      orderDate: value.orderDate,
      notes: value.notes || '',
      status: 'DRAFT',
      items,
      grandTotal: this.grandTotal
    };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private toDateInput(value?: string): string {
    return value ? value.slice(0, 10) : this.today();
  }

  private handleWorkflowSuccess(saved: SalesOrder, message: string): void {
    this.submitting = false;
    this.successMessage = message;
    this.selectedOrder = saved;
    this.loadOrders(saved.id);
  }

  private handleWorkflowError(error: unknown, fallbackMessage: string): void {
    this.submitting = false;
    this.errorMessage = extractApiErrorMessage(error, fallbackMessage);
    debugApiError('OrdersComponent.workflow', error);
  }

  private applyDiscountDelta(target: number | null): void {
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

  private openOrderDetails(order: SalesOrder, navigate: boolean): void {
    this.editOrder(order);
    this.currentMode = 'details';
    this.orderForm.disable({ emitEvent: false });
    if (navigate && order.id) {
      this.router.navigate(['/sales/orders/details', order.id]);
    }
  }
}
