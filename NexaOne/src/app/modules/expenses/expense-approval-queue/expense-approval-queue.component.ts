import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Expense, ExpenseCategoryOption } from '../../../models/expense.model';
import { ExpenseService } from '../../../services/expense.service';
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-expense-approval-queue',
  templateUrl: './expense-approval-queue.component.html',
  styleUrls: ['./expense-approval-queue.component.css']
})
export class ExpenseApprovalQueueComponent implements OnInit {
  expenses: Expense[] = [];
  categories: ExpenseCategoryOption[] = [];
  loading = false;
  errorMessage = '';
  filters = {
    fromDate: '',
    toDate: '',
    categoryId: '' as number | '',
    submittedBy: '',
    amountMin: '' as number | '',
    amountMax: '' as number | ''
  };

  constructor(
    private expenseService: ExpenseService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadQueue();
  }

  loadQueue(): void {
    this.loading = true;
    this.errorMessage = '';
    this.expenseService.getApprovalQueue(this.filters).subscribe({
      next: expenses => {
        this.expenses = expenses;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Approval queue could not be loaded.');
      }
    });
  }

  reset(): void {
    this.filters = { fromDate: '', toDate: '', categoryId: '', submittedBy: '', amountMin: '', amountMax: '' };
    this.loadQueue();
  }

  view(expense: Expense): void {
    if (expense.id) this.router.navigate(['/expenses/details', expense.id]);
  }

  approve(expense: Expense): void {
    if (!expense.id || !confirm(`Approve expense "${expense.expenseNo}"?`)) return;
    this.expenseService.approve(expense.id).subscribe({
      next: () => this.loadQueue(),
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be approved.')
    });
  }

  reject(expense: Expense): void {
    if (!expense.id) return;
    const reason = prompt(`Reject expense "${expense.expenseNo}". Enter reason:`);
    if (!reason?.trim()) return;
    this.expenseService.reject(expense.id, reason).subscribe({
      next: () => this.loadQueue(),
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be rejected.')
    });
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  trackExpense(index: number, expense: Expense): number {
    return expense.id || index;
  }

  private loadCategories(): void {
    this.expenseService.getCategories().subscribe({ next: categories => this.categories = categories });
  }
}
