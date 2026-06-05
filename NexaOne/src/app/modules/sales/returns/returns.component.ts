import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Product } from '../../../models/product.model';
import { SalesCustomer, SalesReturnLineItem } from '../../../models/sales-common.model';
import { SalesInvoice } from '../../../models/sales-invoice.model';
import { SalesReturn } from '../../../models/sales-return.model';
import { ProductService } from '../../../services/product.service';
import { SalesCustomerService } from '../../../services/sales-customer.service';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { SalesReturnService } from '../../../services/sales-return.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-returns',
  templateUrl: './returns.component.html',
  styleUrls: ['./returns.component.css']
})
export class ReturnsComponent implements OnInit {
  returnForm: FormGroup;
  returns: SalesReturn[] = [];
  invoices: SalesInvoice[] = [];
  customers: SalesCustomer[] = [];
  products: Product[] = [];
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  selectedReturn: SalesReturn | null = null;

  constructor(
    private fb: FormBuilder,
    private returnService: SalesReturnService,
    private invoiceService: SalesInvoiceService,
    private customerService: SalesCustomerService,
    private productService: ProductService
  ) {
    this.returnForm = this.fb.group({
      invoiceId: [null, Validators.required],
      customerId: [null, Validators.required],
      returnDate: [this.today(), Validators.required],
      notes: ['', [Validators.maxLength(500)]],
      items: this.fb.array([])
    });
    this.addItem();
  }

  ngOnInit(): void {
    this.loadReturns();
    this.loadReferenceData();
  }

  get items(): FormArray {
    return this.returnForm.get('items') as FormArray;
  }

  get totalAmount(): number {
    return this.items.controls.reduce((sum, control) => sum + Number(control.get('total')?.value || 0), 0);
  }

  loadReturns(selectedId?: number): void {
    this.loading = true;
    this.returnService.getAllReturns().subscribe({
      next: (returns) => {
        this.returns = returns;
        this.selectedReturn = returns.find(item => item.id === selectedId) || returns[0] || null;
        this.loading = false;
      },
      error: (error) => {
        this.returns = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sales returns could not be loaded.');
        debugApiError('ReturnsComponent.loadReturns', error);
      }
    });
  }

  loadReferenceData(): void {
    this.invoiceService.getAllInvoices().subscribe({
      next: invoices => this.invoices = invoices,
      error: error => debugApiError('ReturnsComponent.loadInvoices', error)
    });

    this.customerService.getAllCustomers().subscribe({
      next: customers => this.customers = customers,
      error: error => debugApiError('ReturnsComponent.loadCustomers', error)
    });

    this.productService.getAllProducts().subscribe({
      next: products => this.products = products,
      error: (error) => {
        this.products = [];
        debugApiError('ReturnsComponent.loadProducts', error);
      }
    });
  }

  addItem(item?: Partial<SalesReturnLineItem>): void {
    const group = this.fb.group({
      productId: [item?.productId ?? null, Validators.required],
      quantity: [item?.quantity ?? 1, [Validators.required, Validators.min(0.01)]],
      unitPrice: [item?.unitPrice ?? 0, [Validators.required, Validators.min(0)]],
      total: [{ value: item?.total ?? 0, disabled: true }]
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

  onInvoiceChange(): void {
    const invoiceId = Number(this.returnForm.get('invoiceId')?.value || 0);
    const invoice = this.invoices.find(item => item.id === invoiceId);
    if (!invoice) {
      return;
    }

    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.returnForm.patchValue({
      customerId: invoice.customerId
    });

    if (invoice.items.length === 0) {
      this.addItem();
      return;
    }

    invoice.items.forEach(item => this.addItem({
      productId: item.productId,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      total: item.quantity * item.unitPrice
    }));
  }

  recalculateRow(group: FormGroup): void {
    const quantity = Number(group.get('quantity')?.value || 0);
    const unitPrice = Number(group.get('unitPrice')?.value || 0);
    const total = quantity * unitPrice;
    group.get('total')?.setValue(total, { emitEvent: false });
  }

  saveReturn(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.returnForm.invalid || this.items.length === 0) {
      this.returnForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const payload = this.buildReturnPayload();

    this.returnService.saveReturn(payload).subscribe({
      next: (saved) => {
        this.submitting = false;
        this.successMessage = 'Sales return saved successfully.';
        this.selectedReturn = saved;
        this.resetForm();
        this.loadReturns(saved.id);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Sales return could not be saved.');
        debugApiError('ReturnsComponent.saveReturn', error);
      }
    });
  }

  resetForm(): void {
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }

    this.returnForm.reset({
      invoiceId: null,
      customerId: null,
      returnDate: this.today(),
      notes: ''
    });
    this.addItem();
  }

  hasError(path: string, errorName: string): boolean {
    const control = this.returnForm.get(path);
    return !!control && control.touched && control.hasError(errorName);
  }

  private buildReturnPayload(): SalesReturn {
    const value = this.returnForm.getRawValue();
    const invoice = this.invoices.find(item => item.id === Number(value.invoiceId));
    const customer = this.customers.find(item => item.id === Number(value.customerId));
    const items: SalesReturnLineItem[] = value.items.map((item: any) => ({
      productId: item.productId !== null ? Number(item.productId) : null,
      productName: this.products.find(product => product.id === Number(item.productId))?.productName || 'N/A',
      quantity: Number(item.quantity || 0),
      unitPrice: Number(item.unitPrice || 0),
      total: Number(item.total || 0)
    }));

    return {
      invoiceId: value.invoiceId !== null ? Number(value.invoiceId) : null,
      invoiceNo: invoice?.invoiceNo || '',
      customerId: value.customerId !== null ? Number(value.customerId) : null,
      customerName: customer?.name || '',
      returnDate: value.returnDate,
      notes: value.notes || '',
      items,
      totalAmount: items.reduce((sum, item) => sum + item.total, 0)
    };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
