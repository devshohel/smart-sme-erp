import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Expense, ExpenseCategoryOption, ExpensePaymentMethod } from '../../../models/expense.model';
import { ExpenseService } from '../../../services/expense.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-expense-form',
  templateUrl: './expense-form.component.html',
  styleUrls: ['./expense-form.component.css']
})
export class ExpenseFormComponent implements OnInit {
  expense: Expense = this.emptyExpense();
  categories: ExpenseCategoryOption[] = [];
  saving = false;
  loading = false;
  errorMessage = '';
  expenseId: number | null = null;
  selectedReceiptFile: File | null = null;
  readonly methods: ExpensePaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'OTHER'];
  readonly allowedReceiptTypes = ['image/jpeg', 'image/png', 'image/webp', 'application/pdf'];
  readonly maxReceiptSize = 5 * 1024 * 1024;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private expenseService: ExpenseService
  ) {}

  ngOnInit(): void {
    this.expenseId = Number(this.route.snapshot.paramMap.get('id')) || null;
    this.loadCategories();
    if (this.expenseId) {
      this.loadExpense(this.expenseId);
    }
  }

  save(): void {
    this.saving = true;
    this.errorMessage = '';
    const request$ = this.expenseId
      ? this.expenseService.update(this.expenseId, this.expense, this.selectedReceiptFile)
      : this.expenseService.create(this.expense, this.selectedReceiptFile);
    request$.subscribe({
      next: saved => {
        this.saving = false;
        this.router.navigate(['/expenses/details', saved.id]);
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Expense could not be saved.');
        debugApiError('ExpenseFormComponent.save', error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/expenses']);
  }

  onReceiptSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;
    this.errorMessage = '';
    this.selectedReceiptFile = null;
    if (!file) return;
    if (!this.allowedReceiptTypes.includes(file.type)) {
      this.errorMessage = 'Receipt must be jpg, jpeg, png, webp, or pdf.';
      input.value = '';
      return;
    }
    if (file.size > this.maxReceiptSize) {
      this.errorMessage = 'Receipt must be 5 MB or smaller.';
      input.value = '';
      return;
    }
    this.selectedReceiptFile = file;
  }

  clearReceipt(fileInput: HTMLInputElement): void {
    this.selectedReceiptFile = null;
    fileInput.value = '';
  }

  recalculateTax(): void {
    const net = Number(this.expense.netAmount ?? this.expense.amount ?? 0);
    const rate = this.expense.taxApplicable ? Number(this.expense.taxRate || 0) : 0;
    const tax = this.expense.taxApplicable ? +(net * rate / 100).toFixed(2) : 0;
    this.expense.taxAmount = tax;
    this.expense.grossAmount = +(net + tax).toFixed(2);
    this.expense.amount = this.expense.grossAmount;
  }

  receiptUrl(): string | null {
    return this.expense.receiptUrl || null;
  }

  private loadExpense(id: number): void {
    this.loading = true;
    this.expenseService.getById(id).subscribe({
      next: expense => {
        this.expense = { ...expense };
        this.loading = false;
        if (expense.status !== 'DRAFT') {
          this.errorMessage = 'Only draft expenses can be edited.';
        }
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Expense could not be loaded.');
      }
    });
  }

  private loadCategories(): void {
    this.expenseService.getCategories().subscribe({
      next: categories => this.categories = categories.filter(category => category.status !== 'INACTIVE')
    });
  }

  private emptyExpense(): Expense {
    return {
      expenseDate: new Date().toISOString().slice(0, 10),
      categoryId: null,
      amount: 0,
      netAmount: 0,
      taxApplicable: false,
      taxRate: 0,
      taxAmount: 0,
      grossAmount: 0,
      paymentMethod: 'CASH',
      referenceNo: '',
      notes: '',
      status: 'DRAFT'
    };
  }
}
