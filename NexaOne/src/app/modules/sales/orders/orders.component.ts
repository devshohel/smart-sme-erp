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
  currentMode: 'list' | 'create' | 'edit' | 'details' = 'list';
  searchTerm = '';
  statusFilter = '';
  dateFilter = '';

  readonly statuses: SalesOrderStatus[] = ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CONVERTED', 'CANCELLED'];

  constructor(
    private fb: FormBuilder,
    private orderService: SalesOrderService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private authService: AuthService,
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
    this.loadOrders();
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
        this.loadOrders(id);
      }
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
      subtotal: [{ value: item?.subtotal ?? 0, disabled: true }]
    });

    group.get('quantity')?.valueChanges.subscribe(() => this.recalculateRow(group));
    group.get('unitPrice')?.valueChanges.subscribe(() => this.recalculateRow(group));

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
    const subtotal = quantity * unitPrice;
    group.get('subtotal')?.setValue(subtotal, { emitEvent: false });
  }

  get grandTotal(): number {
    return this.items.controls.reduce((sum, control) => sum + Number(control.get('subtotal')?.value || 0), 0);
  }

  get filteredOrders(): SalesOrder[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.orders.filter(order => {
      const matchesKeyword = !keyword
        || (order.orderNo || '').toLowerCase().includes(keyword)
        || (order.customerName || '').toLowerCase().includes(keyword)
        || (order.warehouseName || '').toLowerCase().includes(keyword);
      const matchesStatus = !this.statusFilter || order.status === this.statusFilter;
      const matchesDate = !this.dateFilter || (order.orderDate || '').slice(0, 10) === this.dateFilter;
      return matchesKeyword && matchesStatus && matchesDate;
    });
  }

  saveOrder(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.orderForm.invalid || this.items.length === 0) {
      this.orderForm.markAllAsTouched();
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

  editOrder(order: SalesOrder): void {
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

    if (!order.items.length) {
      this.addItem();
      return;
    }

    order.items.forEach(item => this.addItem(item));
  }

  resetForm(): void {
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
    this.addItem();
    if (this.currentMode !== 'create') {
      this.router.navigate(['/sales/orders']);
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
    return !!order && !!order.id && ['SUBMITTED', 'PENDING'].includes(order.status) && this.hasPermission('SALES_ORDER_APPROVE');
  }

  canReject(order: SalesOrder | null): boolean {
    return !!order && !!order.id && ['SUBMITTED', 'PENDING'].includes(order.status) && this.hasPermission('SALES_ORDER_REJECT');
  }

  canCancel(order: SalesOrder | null): boolean {
    return !!order && !!order.id && !['CONVERTED', 'CANCELLED'].includes(order.status) && this.hasPermission('SALES_ORDER_CANCEL');
  }

  canConvert(order: SalesOrder | null): boolean {
    return !!order && !!order.id && order.status === 'APPROVED' && this.hasPermission('SALES_ORDER_CONVERT');
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
      next: () => {
        this.submitting = false;
        this.successMessage = 'Sales order converted to invoice successfully.';
        this.loadOrders(this.selectedOrder?.id);
        this.router.navigate(['/sales/invoices']);
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
      grandTotal: items.reduce((sum, item) => sum + item.subtotal, 0)
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
}
