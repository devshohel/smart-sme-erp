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
  readonly methods: ExpensePaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'OTHER'];

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
      ? this.expenseService.update(this.expenseId, this.expense)
      : this.expenseService.create(this.expense);
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
      paymentMethod: 'CASH',
      referenceNo: '',
      notes: '',
      status: 'DRAFT'
    };
  }
}
