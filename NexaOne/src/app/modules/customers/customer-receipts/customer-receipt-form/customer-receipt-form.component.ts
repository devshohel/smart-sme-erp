import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerOption } from '../../../../models/customer.model';
import {
  CustomerReceipt,
  CustomerReceiptAllocation,
  CustomerReceiptAllocationMode,
  CustomerReceiptPaymentMethod,
  UnpaidSalesInvoice
} from '../../../../models/customer-receipt.model';
import { CustomerService } from '../../../../services/customer.service';
import { CustomerReceiptService } from '../../../../services/customer-receipt.service';
import { debugApiError, extractApiErrorMessage } from '../../../../shared/utils/api-error.util';

@Component({
  selector: 'app-customer-receipt-form',
  templateUrl: './customer-receipt-form.component.html',
  styleUrls: ['./customer-receipt-form.component.css']
})
export class CustomerReceiptFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  submitting = false;
  isEditMode = false;
  editable = true;
  receiptId: number | null = null;
  errorMessage = '';
  customerSearchTerm = '';
  customerSuggestions: CustomerOption[] = [];
  unpaidInvoices: UnpaidSalesInvoice[] = [];
  selectedCustomerLabel = '';

  readonly paymentMethods: CustomerReceiptPaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'CHEQUE', 'OTHER'];
  readonly allocationModes: CustomerReceiptAllocationMode[] = ['AUTO', 'MANUAL'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private receiptService: CustomerReceiptService,
    private customerService: CustomerService
  ) {
    this.form = this.fb.group({
      customerId: [null, Validators.required],
      receiptDate: ['', Validators.required],
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
    const customerIdParam = this.route.snapshot.queryParamMap.get('customerId');

    if (idParam) {
      this.isEditMode = true;
      this.receiptId = Number(idParam);
      this.loadReceipt(this.receiptId);
      return;
    }

    this.form.patchValue({
      receiptDate: this.todayValue(),
      allocationMode: 'AUTO',
      paymentMethod: 'CASH'
    });

    if (customerIdParam) {
      const customerId = Number(customerIdParam);
      if (customerId) {
        this.loadCustomer(customerId);
      }
    }
  }

  loadReceipt(id: number): void {
    this.loading = true;
    this.receiptService.getReceiptById(id).subscribe({
      next: receipt => {
        this.patchReceipt(receipt);
        this.editable = receipt.status === 'DRAFT';
        if (!this.editable) {
          this.form.disable({ emitEvent: false });
        }
        if (receipt.customerId) {
          this.loadUnpaidInvoices(receipt.customerId, receipt.allocations || []);
        }
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Customer receipt could not be loaded.');
        debugApiError('CustomerReceiptFormComponent.loadReceipt', error);
      }
    });
  }

  loadCustomer(id: number): void {
    this.customerService.getCustomerById(id).subscribe({
      next: customer => this.selectCustomer(customer.id || id, customer.customerCode || '', customer.name),
      error: error => debugApiError('CustomerReceiptFormComponent.loadCustomer', error)
    });
  }

  searchCustomers(term: string): void {
    this.customerSearchTerm = term;
    this.form.patchValue({ customerId: null });
    this.selectedCustomerLabel = '';
    this.unpaidInvoices = [];
    this.clearAllocations();
    if (!term.trim()) {
      this.customerSuggestions = [];
      return;
    }
    this.customerService.searchCustomers(term).subscribe({
      next: customers => this.customerSuggestions = customers,
      error: error => debugApiError('CustomerReceiptFormComponent.searchCustomers', error)
    });
  }

  selectCustomer(id: number, customerCode: string, name: string): void {
    this.form.patchValue({ customerId: id });
    this.customerSearchTerm = `${customerCode || 'CUS'} - ${name}`;
    this.selectedCustomerLabel = this.customerSearchTerm;
    this.customerSuggestions = [];
    this.loadUnpaidInvoices(id);
  }

  onAllocationModeChange(mode: CustomerReceiptAllocationMode): void {
    this.form.patchValue({ allocationMode: mode });
    if (mode === 'AUTO') {
      this.clearAllocations();
      return;
    }
    const customerId = Number(this.form.get('customerId')?.value || 0);
    if (customerId) {
      this.loadUnpaidInvoices(customerId);
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
      this.errorMessage = 'Allocation cannot exceed receipt amount.';
      return;
    }

    const payload = this.buildPayload();
    this.submitting = true;

    const request$ = this.isEditMode && this.receiptId
      ? this.receiptService.updateReceipt(this.receiptId, payload)
      : this.receiptService.createReceipt(payload);

    request$.subscribe({
      next: receipt => {
        if (postAfterSave && receipt.id) {
          this.receiptService.postReceipt(receipt.id).subscribe({
            next: posted => this.finishSave(posted.id || receipt.id),
            error: error => this.finishError(error, 'Receipt was saved but posting failed.')
          });
          return;
        }
        this.finishSave(receipt.id);
      },
      error: error => this.finishError(error, 'Customer receipt could not be saved.')
    });
  }

  cancel(): void {
    this.router.navigate(['/customers/receipts']);
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
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
    this.router.navigate(['/customers/receipts/details', id || this.receiptId || 0]);
  }

  private finishError(error: unknown, fallback: string): void {
    this.submitting = false;
    this.errorMessage = extractApiErrorMessage(error, fallback);
    debugApiError('CustomerReceiptFormComponent.submit', error);
  }

  private buildPayload(): CustomerReceipt {
    const value = this.form.getRawValue();
    return {
      customerId: Number(value.customerId),
      receiptDate: value.receiptDate,
      amount: value.amount === null || value.amount === '' ? 0 : Number(value.amount),
      allocationMode: value.allocationMode,
      paymentMethod: value.paymentMethod,
      referenceNo: this.optionalTextValue(value.referenceNo),
      notes: this.optionalTextValue(value.notes),
      allocations: this.isManualAllocationMode()
        ? value.allocations
            .filter((allocation: any) => Number(allocation.allocatedAmount || 0) > 0)
            .map((allocation: any) => ({
              salesInvoiceId: Number(allocation.salesInvoiceId),
              allocatedAmount: Number(allocation.allocatedAmount || 0)
            }))
        : []
    };
  }

  private patchReceipt(receipt: CustomerReceipt): void {
    this.form.patchValue({
      customerId: receipt.customerId,
      receiptDate: receipt.receiptDate,
      amount: receipt.amount,
      allocationMode: receipt.allocationMode || 'AUTO',
      paymentMethod: receipt.paymentMethod,
      referenceNo: receipt.referenceNo || '',
      notes: receipt.notes || ''
    });
    this.customerSearchTerm = receipt.customerName ? `${receipt.customerCode || 'CUS'} - ${receipt.customerName}` : '';
    this.selectedCustomerLabel = this.customerSearchTerm;
  }

  private loadUnpaidInvoices(customerId: number, existingAllocations: CustomerReceiptAllocation[] = []): void {
    if (!customerId) {
      return;
    }
    this.receiptService.getUnpaidInvoices(customerId).subscribe({
      next: invoices => {
        const mergedInvoices = [...invoices];
        const existingInvoiceIds = new Set<number>(mergedInvoices.map(invoice => Number(invoice.id)));
        existingAllocations.forEach(allocation => {
          const invoiceId = Number(allocation.salesInvoiceId);
          if (existingInvoiceIds.has(invoiceId)) {
            return;
          }
          mergedInvoices.push({
            id: invoiceId,
            invoiceNo: allocation.invoiceNo,
            saleDate: allocation.invoiceDate || this.todayValue(),
            netTotal: Number(allocation.netTotal || 0),
            paidAmount: Number(allocation.paidAmount || 0),
            dueAmount: Number(allocation.dueAmount || 0),
            paymentStatus: Number(allocation.dueAmount || 0) <= 0
              ? 'PAID'
              : Number(allocation.paidAmount || 0) > 0
                ? 'PARTIAL'
                : 'DUE',
            status: 'CONFIRMED',
            orderId: null,
            orderNo: '',
            customerId,
            warehouseId: null,
            notes: '',
            items: [],
            totalAmount: Number(allocation.netTotal || 0),
            discountAmount: 0,
            taxAmount: 0
          } as UnpaidSalesInvoice);
        });
        this.unpaidInvoices = mergedInvoices;
        this.rebuildAllocationRows(existingAllocations);
      },
      error: error => debugApiError('CustomerReceiptFormComponent.loadUnpaidInvoices', error)
    });
  }

  private rebuildAllocationRows(existingAllocations: CustomerReceiptAllocation[] = []): void {
    this.clearAllocations();
    if (!this.isManualAllocationMode() || !this.unpaidInvoices.length) {
      return;
    }

    const existingMap = new Map<number, number>();
    existingAllocations.forEach(allocation => {
      existingMap.set(Number(allocation.salesInvoiceId), Number(allocation.allocatedAmount || 0));
    });

    this.unpaidInvoices.forEach(invoice => {
      this.allocationControls.push(this.fb.group({
        salesInvoiceId: [invoice.id],
        invoiceNo: [invoice.invoiceNo],
        invoiceDate: [invoice.saleDate],
        netTotal: [invoice.netTotal],
        paidAmount: [invoice.paidAmount],
        dueAmount: [invoice.dueAmount],
        allocatedAmount: [existingMap.get(Number(invoice.id)) || 0, [Validators.min(0)]]
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

  private notBlankValidator(control: AbstractControl): ValidationErrors | null {
    return typeof control.value === 'string' && control.value.trim().length === 0
      ? { required: true }
      : null;
  }
}
