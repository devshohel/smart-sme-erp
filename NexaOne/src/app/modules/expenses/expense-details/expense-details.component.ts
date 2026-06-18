import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Expense } from '../../../models/expense.model';
import { ExpenseService } from '../../../services/expense.service';
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-expense-details',
  templateUrl: './expense-details.component.html',
  styleUrls: ['./expense-details.component.css']
})
export class ExpenseDetailsComponent implements OnInit {
  expense: Expense | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private expenseService: ExpenseService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadExpense(id);
  }

  loadExpense(id: number): void {
    this.loading = true;
    this.expenseService.getById(id).subscribe({
      next: expense => {
        this.expense = expense;
        this.loading = false;
      },
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Expense could not be loaded.');
        this.loading = false;
      }
    });
  }

  back(): void {
    this.router.navigate(['/expenses']);
  }

  edit(): void {
    if (this.expense?.id) this.router.navigate(['/expenses/edit', this.expense.id]);
  }

  post(): void {
    if (!this.expense?.id || !confirm(`Post expense "${this.expense.expenseNo}"?`)) return;
    this.expenseService.post(this.expense.id).subscribe({
      next: expense => this.expense = expense,
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be posted.')
    });
  }

  submit(): void {
    if (!this.expense?.id || !confirm(`Submit expense "${this.expense.expenseNo}" for approval?`)) return;
    this.expenseService.submit(this.expense.id).subscribe({
      next: expense => this.expense = expense,
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be submitted.')
    });
  }

  approve(): void {
    if (!this.expense?.id || !confirm(`Approve expense "${this.expense.expenseNo}"?`)) return;
    this.expenseService.approve(this.expense.id).subscribe({
      next: expense => this.expense = expense,
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be approved.')
    });
  }

  reject(): void {
    if (!this.expense?.id) return;
    const reason = prompt(`Reject expense "${this.expense.expenseNo}". Enter reason:`);
    if (!reason?.trim()) return;
    this.expenseService.reject(this.expense.id, reason).subscribe({
      next: expense => this.expense = expense,
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be rejected.')
    });
  }

  reverse(): void {
    if (!this.expense?.id) return;
    const reversalReason = prompt(`Reverse posted expense "${this.expense.expenseNo}". Enter reason:`);
    if (!reversalReason?.trim()) return;
    this.expenseService.reverse(this.expense.id, reversalReason).subscribe({
      next: expense => this.expense = expense,
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be reversed.')
    });
  }

  cancelExpense(): void {
    if (!this.expense?.id || !confirm(`Cancel expense "${this.expense.expenseNo}"?`)) return;
    this.expenseService.cancel(this.expense.id).subscribe({
      next: expense => this.expense = expense,
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be cancelled.')
    });
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  canSubmit(): boolean {
    return this.expense?.status === 'DRAFT' && this.hasPermission('EXPENSE_SUBMIT');
  }

  canApprove(): boolean {
    return this.expense?.status === 'SUBMITTED' && this.hasPermission('EXPENSE_APPROVE');
  }

  canReject(): boolean {
    return this.expense?.status === 'SUBMITTED' && this.hasPermission('EXPENSE_REJECT');
  }

  canPost(): boolean {
    return this.expense?.status === 'APPROVED' && this.hasPermission('EXPENSE_POST');
  }

  canReverse(): boolean {
    return this.expense?.status === 'POSTED' && this.hasPermission('EXPENSE_REVERSE');
  }
}
