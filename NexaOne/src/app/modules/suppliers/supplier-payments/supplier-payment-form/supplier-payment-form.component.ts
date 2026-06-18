import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierOption } from '../../../../models/supplier.model';
import {
  SupplierPayment,
  SupplierPaymentAllocation,
  SupplierPaymentAllocationMode,
  SupplierPaymentMethod,
  UnpaidPurchaseOrder
} from '../../../../models/supplier-payment.model';
import { SupplierService } from '../../../../services/supplier.service';
import { SupplierPaymentService } from '../../../../services/supplier-payment.service';
import { debugApiError, extractApiErrorMessage } from '../../../../shared/utils/api-error.util';

@Component({
  selector: 'app-supplier-payment-form',
  templateUrl: './supplier-payment-form.component.html',
  styleUrls: ['./supplier-payment-form.component.css']
})
export class SupplierPaymentFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  submitting = false;
  isEditMode = false;
  editable = true;
  paymentId: number | null = null;
  errorMessage = '';
  supplierSearchTerm = '';
  supplierSuggestions: SupplierOption[] = [];
  unpaidPurchases: UnpaidPurchaseOrder[] = [];

  readonly paymentMethods: SupplierPaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'CHEQUE', 'OTHER'];
  readonly allocationModes: SupplierPaymentAllocationMode[] = ['AUTO', 'MANUAL'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: SupplierPaymentService,
    private supplierService: SupplierService
  ) {
    this.form = this.fb.group({
      supplierId: [null, Validators.required],
      paymentDate: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      allocationMode: ['AUTO', Validators.required],
      paymentMethod: ['CASH', Validators.required],
      referenceNo: [''],
      notes: [''],
      allocations: this.fb.array([])
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const supplierIdParam = this.route.snapshot.queryParamMap.get('supplierId');
    if (idParam) {
      this.isEditMode = true;
      this.paymentId = Number(idParam);
      this.loadPayment(this.paymentId);
      return;
    }

    this.form.patchValue({ paymentDate: this.todayValue(), allocationMode: 'AUTO', paymentMethod: 'CASH' });
    if (supplierIdParam) {
      const supplierId = Number(supplierIdParam);
      if (supplierId) {
        this.loadSupplier(supplierId);
      }
    }
  }

  loadPayment(id: number): void {
    this.loading = true;
    this.paymentService.getPaymentById(id).subscribe({
      next: payment => {
        this.patchPayment(payment);
        this.editable = payment.status === 'DRAFT';
        if (!this.editable) {
          this.form.disable({ emitEvent: false });
        }
        if (payment.supplierId) {
          this.loadUnpaidPurchases(payment.supplierId, payment.allocations || []);
        }
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be loaded.');
        debugApiError('SupplierPaymentFormComponent.loadPayment', error);
      }
    });
  }

  loadSupplier(id: number): void {
    this.supplierService.getSupplierById(id).subscribe({
      next: supplier => this.selectSupplier(supplier.id || id, supplier.supplierCode || '', supplier.name),
      error: error => debugApiError('SupplierPaymentFormComponent.loadSupplier', error)
    });
  }

  searchSuppliers(term: string): void {
    this.supplierSearchTerm = term;
    this.form.patchValue({ supplierId: null });
    this.unpaidPurchases = [];
    this.clearAllocations();
    if (!term.trim()) {
      this.supplierSuggestions = [];
      return;
    }
    this.supplierService.getSupplierOptions(term).subscribe({
      next: suppliers => this.supplierSuggestions = suppliers,
      error: error => debugApiError('SupplierPaymentFormComponent.searchSuppliers', error)
    });
  }

  selectSupplier(id: number, supplierCode: string, name: string): void {
    this.form.patchValue({ supplierId: id });
    this.supplierSearchTerm = `${supplierCode || 'SUP'} - ${name}`;
    this.supplierSuggestions = [];
    this.loadUnpaidPurchases(id);
  }

  onAllocationModeChange(): void {
    const mode = this.form.get('allocationMode')?.value as SupplierPaymentAllocationMode;
    this.form.patchValue({ allocationMode: mode });
    if (mode === 'AUTO') {
      this.clearAllocations();
      return;
    }
    const supplierId = Number(this.form.get('supplierId')?.value || 0);
    if (supplierId) {
      this.loadUnpaidPurchases(supplierId);
    }
  }

  saveDraft(): void {
    this.submit(false);
  }

  saveAndPost(): void {
    this.submit(true);
  }

  submit(postAfterSave: boolean): void {
    this.errorMessage = '';
    if (this.form.invalid || !this.editable) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.isManualAllocationMode() && this.manualAllocationTotal() > Number(this.form.get('amount')?.value || 0)) {
      this.errorMessage = 'Allocation cannot exceed payment amount.';
      return;
    }

    const payload = this.buildPayload();
    this.submitting = true;
    const request$ = this.isEditMode && this.paymentId
      ? this.paymentService.updatePayment(this.paymentId, payload)
      : this.paymentService.createPayment(payload);

    request$.subscribe({
      next: payment => {
        if (postAfterSave && payment.id) {
          this.paymentService.postPayment(payment.id).subscribe({
            next: posted => this.finishSave(posted.id || payment.id),
            error: error => this.finishError(error, 'Payment was saved but posting failed.')
          });
          return;
        }
        this.finishSave(payment.id);
      },
      error: error => this.finishError(error, 'Supplier payment could not be saved.')
    });
  }

  cancel(): void {
    this.router.navigate(['/suppliers/payments']);
  }

  get allocationControls(): FormArray {
    return this.form.get('allocations') as FormArray;
  }

  manualAllocationTotal(): number {
    if (!this.isManualAllocationMode()) {
      return 0;
    }
    return this.allocationControls.controls.reduce((sum, control) => sum + Number(control.get('allocatedAmount')?.value || 0), 0);
  }

  isManualAllocationMode(): boolean {
    return this.form.get('allocationMode')?.value === 'MANUAL';
  }

  private finishSave(id?: number | null): void {
    this.submitting = false;
    this.router.navigate(['/suppliers/payments/details', id || this.paymentId || 0]);
  }

  private finishError(error: unknown, fallback: string): void {
    this.submitting = false;
    this.errorMessage = extractApiErrorMessage(error, fallback);
    debugApiError('SupplierPaymentFormComponent.submit', error);
  }

  private buildPayload(): SupplierPayment {
    const value = this.form.getRawValue();
    return {
      supplierId: Number(value.supplierId),
      paymentDate: value.paymentDate,
      amount: value.amount === null || value.amount === '' ? 0 : Number(value.amount),
      allocationMode: value.allocationMode,
      paymentMethod: value.paymentMethod,
      referenceNo: this.optionalTextValue(value.referenceNo),
      notes: this.optionalTextValue(value.notes),
      allocations: this.isManualAllocationMode()
        ? value.allocations
            .filter((allocation: any) => Number(allocation.allocatedAmount || 0) > 0)
            .map((allocation: any) => ({
              purchaseOrderId: Number(allocation.purchaseOrderId),
              allocatedAmount: Number(allocation.allocatedAmount || 0)
            }))
        : []
    };
  }

  private patchPayment(payment: SupplierPayment): void {
    this.form.patchValue({
      supplierId: payment.supplierId,
      paymentDate: payment.paymentDate,
      amount: payment.amount,
      allocationMode: payment.allocationMode || 'AUTO',
      paymentMethod: payment.paymentMethod,
      referenceNo: payment.referenceNo || '',
      notes: payment.notes || ''
    });
    this.supplierSearchTerm = payment.supplierName ? `${payment.supplierCode || 'SUP'} - ${payment.supplierName}` : '';
  }

  private loadUnpaidPurchases(supplierId: number, existingAllocations: SupplierPaymentAllocation[] = []): void {
    if (!supplierId) {
      return;
    }
    this.paymentService.getUnpaidPurchases(supplierId).subscribe({
      next: purchases => {
        const mergedPurchases = [...purchases];
        const existingIds = new Set<number>(mergedPurchases.map(purchase => Number(purchase.id)));
        existingAllocations.forEach(allocation => {
          const purchaseOrderId = Number(allocation.purchaseOrderId);
          if (existingIds.has(purchaseOrderId)) {
            return;
          }
          mergedPurchases.push({
            id: purchaseOrderId,
            purchaseCode: allocation.purchaseCode,
            supplierId,
            supplierName: this.supplierSearchTerm,
            warehouseId: null,
            purchaseDate: this.todayValue(),
            totalAmount: 0,
            discountAmount: 0,
            taxAmount: 0,
            netTotal: Number(allocation.purchaseDueAmount || 0),
            paidAmount: 0,
            dueAmount: Number(allocation.purchaseDueAmount || 0),
            status: 'PARTIAL_PAID',
            items: []
          } as UnpaidPurchaseOrder);
        });
        this.unpaidPurchases = mergedPurchases;
        this.rebuildAllocationRows(existingAllocations);
      },
      error: error => debugApiError('SupplierPaymentFormComponent.loadUnpaidPurchases', error)
    });
  }

  private rebuildAllocationRows(existingAllocations: SupplierPaymentAllocation[] = []): void {
    this.clearAllocations();
    if (!this.isManualAllocationMode() || !this.unpaidPurchases.length) {
      return;
    }
    const existingMap = new Map<number, number>();
    existingAllocations.forEach(allocation => existingMap.set(Number(allocation.purchaseOrderId), Number(allocation.allocatedAmount || 0)));
    this.unpaidPurchases.forEach(purchase => {
      this.allocationControls.push(this.fb.group({
        purchaseOrderId: [purchase.id],
        purchaseCode: [purchase.purchaseCode],
        purchaseDate: [purchase.purchaseDate],
        netTotal: [purchase.netTotal],
        paidAmount: [purchase.paidAmount],
        dueAmount: [purchase.dueAmount],
        allocatedAmount: [existingMap.get(Number(purchase.id)) || 0, [Validators.min(0)]]
      }));
    });
  }

  private clearAllocations(): void {
    while (this.allocationControls.length > 0) {
      this.allocationControls.removeAt(0);
    }
  }

  private optionalTextValue(value: string): string | null {
    const normalized = value?.trim();
    return normalized ? normalized : null;
  }

  private todayValue(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
