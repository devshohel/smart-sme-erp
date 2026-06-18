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
}
