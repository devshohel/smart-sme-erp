import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerOption } from '../../../../models/customer.model';
import { CustomerReceipt, CustomerReceiptPaymentMethod } from '../../../../models/customer-receipt.model';
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
  selectedCustomerLabel = '';
  submitAction: 'draft' | 'post' | null = null;

  readonly paymentMethods: CustomerReceiptPaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'CHEQUE', 'OTHER'];

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
      paymentMethod: ['CASH', Validators.required],
      referenceNo: [''],
      notes: ['']
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
  }

  saveDraft(): void {
    this.submitAction = 'draft';
    this.submit(false);
  }

  saveAndPost(): void {
    this.submitAction = 'post';
    this.submit(true);
  }

  submit(postAfterSave: boolean): void {
    this.errorMessage = '';
    if (this.form.invalid || !this.editable) {
      this.form.markAllAsTouched();
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
      paymentMethod: value.paymentMethod,
      referenceNo: this.optionalTextValue(value.referenceNo),
      notes: this.optionalTextValue(value.notes)
    };
  }

  private patchReceipt(receipt: CustomerReceipt): void {
    this.form.patchValue({
      customerId: receipt.customerId,
      receiptDate: receipt.receiptDate,
      amount: receipt.amount,
      paymentMethod: receipt.paymentMethod,
      referenceNo: receipt.referenceNo || '',
      notes: receipt.notes || ''
    });
    this.customerSearchTerm = receipt.customerName ? `${receipt.customerCode || 'CUS'} - ${receipt.customerName}` : '';
    this.selectedCustomerLabel = this.customerSearchTerm;
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
