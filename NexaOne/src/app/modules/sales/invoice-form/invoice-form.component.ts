import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { Product } from '../../../models/product.model';
import { PaymentStatus, SalesCustomer, SalesInvoiceLineItem } from '../../../models/sales-common.model';
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
  loading = false;
  submitting = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private invoiceService: SalesInvoiceService,
    private orderService: SalesOrderService,
    private customerService: SalesCustomerService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      orderId: [null],
      customerId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      saleDate: [this.today(), Validators.required],
      notes: ['', Validators.maxLength(500)],
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.mode = this.route.snapshot.data['mode'] || 'create';
    this.loadReferenceData();
    const id = Number(this.route.snapshot.paramMap.get('id') || 0);
    if (id) this.loadInvoice(id);
  }

  get items(): FormArray { return this.form.get('items') as FormArray; }
  get totalAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('quantity')?.value || 0) * Number(row.get('unitPrice')?.value || 0), 0); }
  get discountAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('discount')?.value || 0), 0); }
  get taxAmount(): number { return this.items.controls.reduce((sum, row) => sum + Number(row.get('tax')?.value || 0), 0); }
  get netTotal(): number { return this.totalAmount - this.discountAmount + this.taxAmount; }
  get dueAmount(): number { return this.invoice ? Number(this.invoice.dueAmount || 0) : this.netTotal; }
  get paymentStatus(): PaymentStatus { return this.invoice?.paymentStatus || 'DUE'; }

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
    if (product) row.patchValue({ unitPrice: Number(product.salePrice || 0) });
  }

  onOrderChange(): void {
    const order = this.orders.find(item => item.id === Number(this.form.get('orderId')?.value));
    if (!order) return;
    this.clearItems();
    this.form.patchValue({ customerId: order.customerId, warehouseId: order.warehouseId });
    (order.items || []).forEach(item => this.addItem({
      productId: item.productId, quantity: item.quantity, unitPrice: item.unitPrice,
      discount: 0, tax: 0, subtotal: item.subtotal
    }));
    if (!this.items.length) this.addItem();
  }

  save(): void {
    this.errorMessage = '';
    if (this.form.invalid || !this.items.length || (this.mode === 'edit' && !this.canEdit())) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.invoiceService.saveInvoice(this.buildPayload()).subscribe({
      next: saved => this.router.navigate(['/sales', saved.id, 'view']),
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
      this.router.navigate(['/customers/receipts/create'], { queryParams: { customerId: this.invoice.customerId } });
    }
  }

  createReturn(): void {
    if (this.canReturn()) this.router.navigate(['/sales/returns/create'], { queryParams: { invoiceId: this.invoice?.id } });
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
    this.customerService.getAllCustomers().subscribe({ next: customers => this.customers = customers, error: error => debugApiError('InvoiceForm.customers', error) });
    this.productService.getAllProducts().subscribe({ next: products => this.products = products, error: error => debugApiError('InvoiceForm.products', error) });
    this.warehouseService.getAllWarehouses().subscribe({ next: warehouses => this.warehouses = warehouses, error: error => debugApiError('InvoiceForm.warehouses', error) });
  }

  private applyInvoice(invoice: SalesInvoice): void {
    this.invoice = invoice;
    this.clearItems();
    this.form.patchValue({ orderId: invoice.orderId ?? null, customerId: invoice.customerId, warehouseId: invoice.warehouseId, saleDate: (invoice.saleDate || '').slice(0, 10), notes: invoice.notes || '' });
    (invoice.items || []).forEach(item => this.addItem(item));
    if (!this.items.length) this.addItem();
    if (this.mode === 'details') this.form.disable({ emitEvent: false });
  }

  private buildPayload(): SalesInvoice {
    const value = this.form.getRawValue();
    return {
      id: this.mode === 'edit' ? this.invoice?.id : undefined,
      orderId: value.orderId ? Number(value.orderId) : null,
      customerId: Number(value.customerId), warehouseId: Number(value.warehouseId), saleDate: value.saleDate,
      notes: value.notes || '', totalAmount: this.totalAmount, discountAmount: this.discountAmount,
      taxAmount: this.taxAmount, netTotal: this.netTotal, paidAmount: Number(this.invoice?.paidAmount || 0),
      dueAmount: this.mode === 'edit' ? this.dueAmount : this.netTotal, paymentStatus: this.paymentStatus,
      status: this.mode === 'edit' ? this.invoice?.status : 'DRAFT',
      items: value.items.map((item: any, index: number) => ({
        productId: Number(item.productId), quantity: Number(item.quantity), unitPrice: Number(item.unitPrice),
        discount: Number(item.discount), tax: Number(item.tax), subtotal: this.rowSubtotal(index)
      }))
    };
  }

  private clearItems(): void { while (this.items.length) this.items.removeAt(0); }
  private today(): string { return new Date().toISOString().slice(0, 10); }
  private isCompleted(): boolean { return ['POSTED', 'PARTIAL_PAID', 'PAID', 'COMPLETED'].includes(this.invoice?.status || ''); }
}
