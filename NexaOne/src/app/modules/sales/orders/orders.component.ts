import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Product } from '../../../models/product.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { SalesCustomer, SalesOrderLineItem, SalesOrderStatus } from '../../../models/sales-common.model';
import { SalesOrder } from '../../../models/sales-order.model';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { SalesOrderService } from '../../../services/sales-order.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

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

  readonly statuses: SalesOrderStatus[] = ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

  constructor(
    private fb: FormBuilder,
    private orderService: SalesOrderService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService
  ) {
    this.orderForm = this.fb.group({
      customerId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      orderDate: [this.today(), Validators.required],
      status: ['PENDING', Validators.required],
      notes: ['', [Validators.maxLength(500)]],
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.loadOrders();
    this.loadReferenceData();
  }

  get items(): FormArray {
    return this.orderForm.get('items') as FormArray;
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.selectedOrder = orders[0] || null;
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

  addItem(): void {
    const group = this.fb.group({
      productId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      subtotal: [{ value: 0, disabled: true }]
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
        this.successMessage = 'Sales order saved successfully.';
        this.selectedOrder = saved;
        this.loadOrders();
        this.resetForm();
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
  }

  resetForm(): void {
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.orderForm.reset({
      customerId: null,
      warehouseId: null,
      orderDate: this.today(),
      status: 'PENDING',
      notes: ''
    });
    this.addItem();
  }

  productName(productId: number | null): string {
    return this.products.find(item => item.id === productId)?.productName || 'N/A';
  }

  hasError(path: string, errorName: string): boolean {
    const control = this.orderForm.get(path);
    return !!control && control.touched && control.hasError(errorName);
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
      customerId: value.customerId !== null ? Number(value.customerId) : null,
      customerName: customer?.name || '',
      warehouseId: value.warehouseId !== null ? Number(value.warehouseId) : null,
      warehouseName: warehouse?.name || '',
      orderDate: value.orderDate,
      notes: value.notes || '',
      status: value.status,
      items,
      grandTotal: items.reduce((sum, item) => sum + item.subtotal, 0)
    };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
