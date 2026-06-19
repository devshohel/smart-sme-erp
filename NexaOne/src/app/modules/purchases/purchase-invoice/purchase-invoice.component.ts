import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PurchaseOrder, PurchaseReceive, PurchaseReceiveItem, PurchaseStatus } from '../../../models/purchase.model';
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
  receives: PurchaseReceive[] = [];
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
      items: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.loadOrders();
    this.loadReceives();
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  get eligibleOrders(): PurchaseOrder[] {
    return this.orders.filter(order => order.status === 'APPROVED' || order.status === 'PARTIAL_RECEIVED');
  }

  get invoiceList(): PurchaseOrder[] {
    return this.orders.filter(order => ['RECEIVED', 'PARTIAL_PAID', 'PAID'].includes(order.status));
  }

  get netTotal(): number {
    return Number(this.selectedOrder?.netTotal || 0);
  }

  get totalReceivingQty(): number {
    return this.items.controls.reduce((sum, control) => sum + Number(control.get('receivedQty')?.value || 0), 0);
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
        this.errorMessage = extractApiErrorMessage(error, 'Purchase receives could not be loaded.');
        debugApiError('PurchaseInvoiceComponent.loadOrders', error);
      }
    });
  }

  loadReceives(): void {
    this.purchaseService.getAllReceives().subscribe({
      next: (receives) => this.receives = receives,
      error: (error) => debugApiError('PurchaseInvoiceComponent.loadReceives', error)
    });
  }

  onPurchaseChange(): void {
    const purchaseId = Number(this.form.get('purchaseId')?.value || 0);
    const order = this.orders.find(item => item.id === purchaseId);
    this.selectedOrder = order || null;
    if (order) {
      this.patchSelection(order);
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

    const payload: PurchaseReceive = {
      purchaseOrderId: this.selectedOrder.id || null,
      receiveDate: this.form.get('receiveDate')?.value,
      items: this.items.controls
        .map(control => ({
          productId: Number(control.get('productId')?.value || 0),
          receivedQty: Number(control.get('receivedQty')?.value || 0)
        } as PurchaseReceiveItem))
        .filter(item => item.receivedQty > 0)
    };

    if (!payload.items.length) {
      this.errorMessage = 'Enter at least one receive quantity greater than zero.';
      return;
    }

    this.submitting = true;
    this.purchaseService.receiveOrder(this.selectedOrder.id!, payload).subscribe({
      next: (saved) => {
        this.submitting = false;
        this.successMessage = 'Goods receive posted successfully.';
        this.selectedOrder = saved;
        this.loadReceives();
        this.loadOrders(saved.id);
      },
      error: (error) => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Goods receive could not be posted.');
        debugApiError('PurchaseInvoiceComponent.receiveAndSaveInvoice', error);
      }
    });
  }

  statusClass(status?: PurchaseStatus): string {
    switch (status) {
      case 'RECEIVED':
      case 'PAID':
        return 'bg-success-subtle text-success';
      case 'PARTIAL_RECEIVED':
      case 'PARTIAL_PAID':
        return 'bg-info-subtle text-info';
      case 'APPROVED':
        return 'bg-warning-subtle text-warning';
      case 'CANCELLED':
        return 'bg-danger-subtle text-danger';
      default:
        return 'bg-secondary-subtle text-secondary';
    }
  }

  hasError(path: string, errorName: string): boolean {
    const control = this.form.get(path);
    return !!control && control.touched && control.hasError(errorName);
  }

  private patchSelection(order: PurchaseOrder): void {
    while (this.items.length > 0) {
      this.items.removeAt(0);
    }
    order.items.forEach(item => {
      const ordered = Number(item.quantity || 0);
      const alreadyReceived = Number(item.receivedQuantity || 0);
      const remaining = Math.max(ordered - alreadyReceived, 0);
      this.items.push(this.fb.group({
        productId: [item.productId, Validators.required],
        productName: [item.productName || ''],
        orderedQty: [ordered],
        alreadyReceivedQty: [alreadyReceived],
        remainingQty: [remaining],
        receivedQty: [remaining, [Validators.required, Validators.min(0)]]
      }));
    });

    this.form.patchValue({
      purchaseId: order.id || null,
      receiveDate: this.today()
    }, { emitEvent: false });
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
