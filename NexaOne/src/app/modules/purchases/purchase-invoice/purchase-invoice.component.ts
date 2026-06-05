import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PurchaseOrder, PurchaseStatus } from '../../../models/purchase.model';
import { PurchaseService } from '../../../services/purchase.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-purchase-invoice',
  templateUrl: './purchase-invoice.component.html',
  styleUrls: ['./purchase-invoice.component.css']
})
export class PurchaseInvoiceComponent implements OnInit {
  form: FormGroup;
  orders: PurchaseOrder[] = [];
  selectedOrder: PurchaseOrder | null = null;
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private purchaseService: PurchaseService
  ) {
    this.form = this.fb.group({
      purchaseId: [null, Validators.required],
      receiveDate: [this.today(), Validators.required],
      paidAmount: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.loadOrders();
  }

  get eligibleOrders(): PurchaseOrder[] {
    return this.orders.filter(order => order.status === 'APPROVED' || order.status === 'RECEIVED');
  }

  get invoiceList(): PurchaseOrder[] {
    return this.orders.filter(order => order.status === 'RECEIVED' || Number(order.paidAmount || 0) > 0);
  }

  get netTotal(): number {
    return Number(this.selectedOrder?.netTotal || 0);
  }

  get paidAmount(): number {
    return Number(this.form.get('paidAmount')?.value || 0);
  }

  get dueAmount(): number {
    return Math.max(this.netTotal - this.paidAmount, 0);
  }

  get advanceAmount(): number {
    return Math.max(this.paidAmount - this.netTotal, 0);
  }

  loadOrders(selectedId?: number): void {
    this.loading = true;
    this.purchaseService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.selectedOrder = this.orders.find(order => order.id === selectedId) || this.eligibleOrders[0] || null;
        if (this.selectedOrder) {
          this.patchSelection(this.selectedOrder);
        }
        this.loading = false;
      },
      error: (error) => {
        this.orders = [];
        this.selectedOrder = null;
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Purchase invoices could not be loaded.');
        debugApiError('PurchaseInvoiceComponent.loadOrders', error);
      }
    });
  }

  onPurchaseChange(): void {
    const purchaseId = Number(this.form.get('purchaseId')?.value || 0);
    const order = this.orders.find(item => item.id === purchaseId);
    this.selectedOrder = order || null;
    if (order) {
      this.form.patchValue({ paidAmount: order.paidAmount || 0 }, { emitEvent: false });
    }
  }

  selectInvoice(order: PurchaseOrder): void {
    this.selectedOrder = order;
    this.patchSelection(order);
  }

  receiveAndSaveInvoice(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid || !this.selectedOrder) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const payload: PurchaseOrder = {
      ...this.selectedOrder,
      status: 'RECEIVED',
      paidAmount: this.paidAmount,
      dueAmount: this.dueAmount
    };

    this.purchaseService.saveOrder(payload).subscribe({
      next: (saved) => {
        this.submitting = false;
        this.successMessage = 'Purchase invoice saved and goods receive completed.';
        this.selectedOrder = saved;
        this.loadOrders(saved.id);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Purchase invoice could not be saved.');
        debugApiError('PurchaseInvoiceComponent.receiveAndSaveInvoice', error);
      }
    });
  }

  statusClass(status?: PurchaseStatus): string {
    switch (status) {
      case 'RECEIVED':
      case 'PAID':
        return 'bg-success-subtle text-success';
      case 'APPROVED':
      case 'PARTIAL_PAID':
        return 'bg-info-subtle text-info';
      case 'CANCELLED':
        return 'bg-danger-subtle text-danger';
      default:
        return 'bg-warning-subtle text-warning';
    }
  }

  hasError(path: string, errorName: string): boolean {
    const control = this.form.get(path);
    return !!control && control.touched && control.hasError(errorName);
  }

  private patchSelection(order: PurchaseOrder): void {
    this.form.patchValue({
      purchaseId: order.id || null,
      receiveDate: this.today(),
      paidAmount: order.paidAmount || 0
    }, { emitEvent: false });
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
