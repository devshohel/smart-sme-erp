import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SalesReturnCondition, SalesReturnLineItem, SalesReturnStatus } from '../../../models/sales-common.model';
import { SalesInvoice } from '../../../models/sales-invoice.model';
import { SalesReturn, SalesReturnContext, SalesReturnContextItem } from '../../../models/sales-return.model';
import { SalesInvoiceService } from '../../../services/sales-invoice.service';
import { SalesReturnService } from '../../../services/sales-return.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({ selector: 'app-returns', templateUrl: './returns.component.html', styleUrls: ['./returns.component.css'] })
export class ReturnsComponent implements OnInit {
  returnForm: FormGroup;
  returns: SalesReturn[] = [];
  invoices: SalesInvoice[] = [];
  returnContext: SalesReturnContext | null = null;
  selectedReturn: SalesReturn | null = null;
  editingReturnId: number | null = null;
  currentMode: 'list' | 'create' | 'edit' | 'details' = 'list';
  loading = false;
  submitting = false;
  successMessage = '';
  errorMessage = '';
  searchTerm = '';
  statusFilter = '';
  dateFilter = '';
  invoiceSearchTerm = '';
  readonly statuses: SalesReturnStatus[] = ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'POSTED', 'CANCELLED', 'REVERSED'];
  readonly conditions: SalesReturnCondition[] = ['RESELLABLE', 'DAMAGED', 'EXPIRED'];

  constructor(private fb: FormBuilder, private returnService: SalesReturnService,
    private invoiceService: SalesInvoiceService, private authService: AuthService,
    private route: ActivatedRoute, private router: Router) {
    this.returnForm = this.fb.group({
      invoiceId: [null, Validators.required],
      customerId: [{ value: null, disabled: true }, Validators.required],
      returnDate: [this.today(), Validators.required],
      refundMethod: ['ADJUST_DUE', Validators.required],
      notes: ['', Validators.maxLength(500)],
      items: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.currentMode = this.route.snapshot.data['mode'] || 'list';
    this.loadInvoices();
    const id = Number(this.route.snapshot.paramMap.get('id') || 0);
    if (this.currentMode !== 'create') this.loadReturns(id || undefined);
    if (this.currentMode === 'create') {
      const invoiceId = Number(this.route.snapshot.queryParamMap.get('invoiceId') || 0);
      if (invoiceId > 0) { this.returnForm.patchValue({ invoiceId }); this.loadContext(invoiceId); }
    }
  }

  get items(): FormArray { return this.returnForm.get('items') as FormArray; }
  get totalAmount(): number { return this.items.controls.reduce((sum, c) => sum + Number(c.get('total')?.value || 0), 0); }
  get filteredReturns(): SalesReturn[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    return this.returns.filter(item => {
      const matches = !keyword || (item.returnNo || item.returnCode || '').toLowerCase().includes(keyword)
        || (item.invoiceNo || '').toLowerCase().includes(keyword) || (item.customerName || '').toLowerCase().includes(keyword);
      return matches && (!this.statusFilter || item.status === this.statusFilter)
        && (!this.dateFilter || (item.returnDate || '').slice(0, 10) === this.dateFilter);
    });
  }

  get filteredInvoices(): SalesInvoice[] {
    const keyword = this.invoiceSearchTerm.trim().toLowerCase();
    return this.invoices.filter(invoice => !keyword
      || (invoice.invoiceNo || '').toLowerCase().includes(keyword)
      || (invoice.customerName || '').toLowerCase().includes(keyword));
  }

  loadReturns(selectedId?: number): void {
    this.loading = true;
    this.returnService.getAllReturns().subscribe({
      next: returns => {
        this.returns = returns;
        this.selectedReturn = selectedId ? returns.find(item => item.id === selectedId) || null : null;
        this.loading = false;
        if (selectedId && !this.selectedReturn) this.errorMessage = 'Sales return was not found.';
        if (this.currentMode === 'edit' && this.selectedReturn?.invoiceId) {
          this.editingReturnId = this.selectedReturn.id || null;
          this.loadContext(this.selectedReturn.invoiceId, this.selectedReturn);
        }
        if (this.currentMode === 'details' && this.selectedReturn && this.route.snapshot.queryParamMap.get('print') === 'true') {
          setTimeout(() => window.print());
        }
      },
      error: error => { this.loading = false; this.errorMessage = extractApiErrorMessage(error, 'Sales returns could not be loaded.'); }
    });
  }

  loadInvoices(): void {
    this.invoiceService.getAllInvoices().subscribe({
      next: invoices => this.invoices = invoices.filter(i => ['POSTED', 'CLOSED', 'PARTIAL_PAID', 'PAID', 'CONFIRMED', 'COMPLETED'].includes(i.status || '')),
      error: error => debugApiError('ReturnsComponent.loadInvoices', error)
    });
  }

  onInvoiceChange(): void {
    const invoiceId = Number(this.returnForm.get('invoiceId')?.value || 0);
    this.returnContext = null; this.clearItems();
    if (invoiceId > 0) this.loadContext(invoiceId);
  }

  loadContext(invoiceId: number, existing?: SalesReturn): void {
    this.loading = true; this.errorMessage = '';
    this.returnService.getReturnContext(invoiceId).subscribe({
      next: context => {
        this.loading = false; this.returnContext = context;
        this.returnForm.patchValue({ invoiceId: context.invoiceId, customerId: context.customerId,
          returnDate: existing ? this.toDateInput(existing.returnDate) : this.today(),
          refundMethod: existing?.refundMethod || 'ADJUST_DUE', notes: existing?.notes || '' });
        this.clearItems();
        context.items.forEach(line => this.addContextItem(line, existing));
      },
      error: error => { this.loading = false; this.errorMessage = extractApiErrorMessage(error, 'Invoice return context could not be loaded.'); }
    });
  }

  addContextItem(line: SalesReturnContextItem, existing?: SalesReturn): void {
    const saved = existing?.items.find(item => item.invoiceItemId === line.invoiceItemId);
    const group = this.fb.group({
      invoiceItemId: [line.invoiceItemId], productId: [line.productId], productName: [line.productName],
      soldQuantity: [line.soldQuantity], alreadyReturnedQuantity: [line.returnedQuantity], remainingQuantity: [line.remainingQuantity],
      quantity: [saved?.quantity || 0, [Validators.min(0), Validators.max(line.remainingQuantity)]],
      unitPrice: [line.unitPrice], discount: [line.discount], tax: [line.tax],
      returnReason: [saved?.returnReason || '', Validators.maxLength(500)],
      condition: [saved?.condition || 'RESELLABLE'], restock: [saved?.restock !== false], total: [{ value: 0, disabled: true }]
    });
    group.get('quantity')?.valueChanges.subscribe(() => this.recalculateRow(group));
    group.get('condition')?.valueChanges.subscribe(value => { if (value !== 'RESELLABLE') group.get('restock')?.setValue(false); });
    this.items.push(group); this.recalculateRow(group);
  }

  recalculateRow(group: FormGroup): void {
    const quantity = Number(group.get('quantity')?.value || 0), sold = Number(group.get('soldQuantity')?.value || 0);
    const gross = quantity * Number(group.get('unitPrice')?.value || 0);
    const discount = sold > 0 ? Number(group.get('discount')?.value || 0) * quantity / sold : 0;
    const tax = sold > 0 ? Number(group.get('tax')?.value || 0) * quantity / sold : 0;
    group.get('total')?.setValue(Math.max(0, gross - discount + tax), { emitEvent: false });
  }

  saveReturn(): void {
    this.successMessage = ''; this.errorMessage = '';
    const selected = this.items.controls.filter(c => Number(c.get('quantity')?.value || 0) > 0);
    if (this.returnForm.invalid || selected.length === 0 || !this.returnContext) {
      this.returnForm.markAllAsTouched();
      if (selected.length === 0) this.errorMessage = 'Enter a return quantity for at least one invoice item.';
      return;
    }
    this.submitting = true;
    this.returnService.saveReturn(this.buildReturnPayload()).subscribe({
      next: saved => {
        if (saved.id && this.hasPermission('SALES_RETURN_SUBMIT')) {
          this.returnService.submitReturn(saved.id).subscribe({
            next: submitted => { this.submitting = false; this.router.navigate(['/sales/returns', submitted.id, 'view']); },
            error: error => { this.submitting = false; this.errorMessage = extractApiErrorMessage(error, 'Return was saved as draft but could not be submitted.'); }
          });
          return;
        }
        this.submitting = false;
        this.router.navigate(['/sales/returns', saved.id, 'view']);
      },
      error: error => { this.submitting = false; this.errorMessage = extractApiErrorMessage(error, 'Sales return could not be saved.'); }
    });
  }

  editSelected(): void { if (this.selectedReturn?.id) this.router.navigate(['/sales/returns/edit', this.selectedReturn.id]); }
  viewReturn(value: SalesReturn): void { if (value.id) this.router.navigate(['/sales/returns', value.id, 'view']); }
  editReturn(value: SalesReturn): void { if (this.canEdit(value) && value.id) this.router.navigate(['/sales/returns/edit', value.id]); }
  approveReturn(value: SalesReturn): void {
    if (!this.canApprove(value) || !value.id) return;
    this.selectedReturn = value;
    this.submitting = true;
    this.returnService.approveReturn(value.id).subscribe({
      next: approved => this.returnService.postReturn(value.id!).subscribe({
        next: posted => {
          this.submitting = false;
          this.selectedReturn = posted;
          this.successMessage = 'Sales return approved and posted successfully.';
          this.loadInvoices();
          this.loadReturns(posted.id);
        },
        error: error => {
          this.submitting = false;
          this.selectedReturn = approved;
          this.errorMessage = extractApiErrorMessage(error, 'Return was approved, but backend posting could not be completed.');
          this.loadReturns(approved.id);
        }
      }),
      error: error => { this.submitting = false; this.errorMessage = extractApiErrorMessage(error, 'Return approval failed.'); }
    });
  }
  rejectReturn(value: SalesReturn): void {
    if (!this.canReject(value) || !value.id) return;
    const reason = window.prompt('Enter rejection reason');
    if (reason?.trim()) {
      this.selectedReturn = value;
      this.runRequest(this.returnService.rejectReturn(value.id, reason.trim()), 'Sales return rejected successfully.');
    }
  }
  printReturn(value: SalesReturn): void {
    if (value.id && this.hasPermission('SALES_RETURN_PRINT')) {
      this.router.navigate(['/sales/returns', value.id, 'view'], { queryParams: { print: true } });
    }
  }
  hasPermission(permission: string): boolean { return this.authService.hasPermission(permission); }
  returnStatusLabel(value: SalesReturn): string { return value.status === 'SUBMITTED' ? 'PENDING' : (value.status || 'DRAFT'); }
  canEdit(v: SalesReturn | null): boolean { return !!v?.id && ['DRAFT', 'REJECTED'].includes(v.status || '') && this.hasPermission('SALES_RETURN_EDIT'); }
  canSubmit(v: SalesReturn | null): boolean { return !!v?.id && ['DRAFT', 'REJECTED'].includes(v.status || '') && this.hasPermission('SALES_RETURN_SUBMIT'); }
  canApprove(v: SalesReturn | null): boolean { return !!v?.id && v.status === 'SUBMITTED' && this.hasPermission('SALES_RETURN_APPROVE'); }
  canReject(v: SalesReturn | null): boolean { return !!v?.id && v.status === 'SUBMITTED' && this.hasPermission('SALES_RETURN_REJECT'); }
  canPost(v: SalesReturn | null): boolean { return !!v?.id && v.status === 'APPROVED' && this.hasPermission('SALES_RETURN_POST'); }
  canCancel(v: SalesReturn | null): boolean { return !!v?.id && !['POSTED', 'CANCELLED'].includes(v.status || '') && this.hasPermission('SALES_RETURN_CANCEL'); }
  submitSelected(): void { this.runAction('submit', 'Sales return submitted successfully.'); }
  approveSelected(): void { this.runAction('approve', 'Sales return approved successfully.'); }
  postSelected(): void { this.runAction('post', 'Sales return posted successfully.'); }
  rejectSelected(): void { const reason = window.prompt('Enter rejection reason'); if (reason?.trim() && this.selectedReturn?.id) this.runRequest(this.returnService.rejectReturn(this.selectedReturn.id, reason.trim()), 'Sales return rejected successfully.'); }
  cancelSelected(): void { const reason = window.prompt('Enter cancellation reason'); if (reason?.trim() && this.selectedReturn?.id) this.runRequest(this.returnService.cancelReturn(this.selectedReturn.id, reason.trim()), 'Sales return cancelled successfully.'); }
  printSelected(): void { window.print(); }
  backToList(): void { this.router.navigate(['/sales/returns']); }

  private runAction(action: 'submit' | 'approve' | 'post', message: string): void {
    if (!this.selectedReturn?.id) return;
    const request = action === 'submit' ? this.returnService.submitReturn(this.selectedReturn.id)
      : action === 'approve' ? this.returnService.approveReturn(this.selectedReturn.id) : this.returnService.postReturn(this.selectedReturn.id);
    this.runRequest(request, message);
  }
  private runRequest(request: ReturnType<SalesReturnService['submitReturn']>, message: string): void {
    this.submitting = true;
    request.subscribe({ next: saved => {
        this.submitting = false; this.selectedReturn = saved; this.successMessage = message;
        this.loadInvoices();
        this.loadReturns(saved.id);
      },
      error: error => { this.submitting = false; this.errorMessage = extractApiErrorMessage(error, 'Return action failed.'); } });
  }
  private buildReturnPayload(): SalesReturn {
    const value = this.returnForm.getRawValue();
    const items: SalesReturnLineItem[] = value.items.filter((i: SalesReturnLineItem) => Number(i.quantity) > 0)
      .map((i: SalesReturnLineItem) => ({ ...i, productId: Number(i.productId), invoiceItemId: Number(i.invoiceItemId),
        quantity: Number(i.quantity), unitPrice: Number(i.unitPrice), total: Number(i.total || 0) }));
    return { id: this.editingReturnId || undefined, invoiceId: this.returnContext!.invoiceId, invoiceNo: this.returnContext!.invoiceNo,
      customerId: this.returnContext!.customerId, customerName: this.returnContext!.customerName,
      warehouseId: this.returnContext!.warehouseId, warehouseName: this.returnContext!.warehouseName,
      returnDate: value.returnDate, refundMethod: 'ADJUST_DUE', notes: value.notes || '', status: 'DRAFT', items,
      totalAmount: items.reduce((sum, item) => sum + item.total, 0) };
  }
  private clearItems(): void { while (this.items.length) this.items.removeAt(0); }
  private today(): string { return new Date().toISOString().slice(0, 10); }
  private toDateInput(value?: string): string { return value ? value.slice(0, 10) : this.today(); }
}
