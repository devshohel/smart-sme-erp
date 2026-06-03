import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { Product } from '../../../models/product.model';
import { PaymentStatus, SalesCustomer, SalesInvoiceLineItem } from '../../../models/sales-common.model';
import { SalesInvoice } from '../../../models/sales-invoice.model';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-invoices',
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {
  invoiceForm: FormGroup;
  invoices: SalesInvoice[] = [];
  customers: SalesCustomer[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  selectedInvoice: SalesInvoice | null = null;

  readonly paymentStatuses: PaymentStatus[] = ['PAID', 'PARTIAL', 'DUE'];

  constructor(
    private fb: FormBuilder,
    private invoiceService: SalesInvoiceService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService
  ) {
    this.invoiceForm = this.fb.group({
      customerId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      saleDate: [this.today(), Validators.required],
      paidAmount: [0, [Validators.required, Validators.min(0)]],
      notes: ['', [Validators.maxLength(500)]],
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.loadInvoices();
    this.loadReferenceData();
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

  loadInvoices(): void {
    this.loading = true;
    this.invoiceService.getAllInvoices().subscribe({
      next: (invoices) => {
        this.invoices = invoices;
        this.selectedInvoice = invoices[0] || null;
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

  addItem(): void {
    const group = this.fb.group({
      productId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      discount: [0, [Validators.required, Validators.min(0)]],
      tax: [0, [Validators.required, Validators.min(0)]],
      subtotal: [{ value: 0, disabled: true }]
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
        this.successMessage = 'Invoice saved successfully.';
        this.selectedInvoice = saved;
        this.loadInvoices();
        this.resetForm();
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

  resetForm(): void {
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.invoiceForm.reset({
      customerId: null,
      warehouseId: null,
      saleDate: this.today(),
      paidAmount: 0,
      notes: ''
    });
    this.addItem();
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
      paidAmount: Number(value.paidAmount || 0),
      dueAmount: this.dueAmount,
      paymentStatus: this.paymentStatus
    };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
