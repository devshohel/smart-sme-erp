import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Expense, ExpenseCategoryOption, ExpensePaymentMethod, ExpenseStatus } from '../../../models/expense.model';
import { ExpenseService } from '../../../services/expense.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-expense-list',
  templateUrl: './expense-list.component.html',
  styleUrls: ['./expense-list.component.css']
})
export class ExpenseListComponent implements OnInit {
  expenses: Expense[] = [];
  categories: ExpenseCategoryOption[] = [];
  loading = false;
  errorMessage = '';
  filters = {
    keyword: '',
    categoryId: '' as number | '',
    paymentMethod: '' as ExpensePaymentMethod | '',
    status: '' as ExpenseStatus | '',
    fromDate: '',
    toDate: '',
    page: 0,
    size: 10,
    sort: 'expenseDate',
    direction: 'desc' as 'asc' | 'desc'
  };
  totalElements = 0;
  totalPages = 1;
  readonly pageSizes = [10, 25, 50, 100];
  readonly methods: ExpensePaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'OTHER'];
  readonly statuses: ExpenseStatus[] = ['DRAFT', 'POSTED', 'CANCELLED'];

  constructor(
    private expenseService: ExpenseService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.loading = true;
    this.errorMessage = '';
    this.expenseService.getPage(this.filters).subscribe({
      next: page => {
        this.expenses = page.content;
        this.totalElements = page.totalElements;
        this.totalPages = Math.max(page.totalPages, 1);
        this.filters.page = page.page;
        this.filters.size = page.size;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Expenses could not be loaded.');
        debugApiError('ExpenseListComponent.loadExpenses', error);
      }
    });
  }

  search(): void {
    this.filters.page = 0;
    this.loadExpenses();
  }

  reset(): void {
    this.filters = { keyword: '', categoryId: '', paymentMethod: '', status: '', fromDate: '', toDate: '', page: 0, size: 10, sort: 'expenseDate', direction: 'desc' };
    this.loadExpenses();
  }

  create(): void {
    this.router.navigate(['/expenses/create']);
  }

  view(expense: Expense): void {
    if (expense.id) this.router.navigate(['/expenses/details', expense.id]);
  }

  edit(expense: Expense): void {
    if (expense.id) this.router.navigate(['/expenses/edit', expense.id]);
  }

  post(expense: Expense): void {
    if (!expense.id || !confirm(`Post expense "${expense.expenseNo}"?`)) return;
    this.expenseService.post(expense.id).subscribe({
      next: () => this.loadExpenses(),
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be posted.')
    });
  }

  cancel(expense: Expense): void {
    if (!expense.id || !confirm(`Cancel expense "${expense.expenseNo}"?`)) return;
    this.expenseService.cancel(expense.id).subscribe({
      next: () => this.loadExpenses(),
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Expense could not be cancelled.')
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.filters.page) return;
    this.filters.page = page;
    this.loadExpenses();
  }

  changePageSize(): void {
    this.filters.page = 0;
    this.loadExpenses();
  }

  exportCsv(): void {
    const rows = [
      ['No', 'Date', 'Category', 'Payment', 'Reference', 'Amount', 'Status'],
      ...this.expenses.map(expense => [
        expense.expenseNo || '',
        expense.expenseDate || '',
        expense.categoryName || '',
        expense.paymentMethod || '',
        expense.referenceNo || '',
        Number(expense.amount || 0).toFixed(2),
        expense.status || ''
      ])
    ];
    const csv = rows.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'expenses.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  canEdit(expense: Expense): boolean {
    return expense.status === 'DRAFT' && this.hasPermission('EXPENSE_EDIT');
  }

  canPost(expense: Expense): boolean {
    return expense.status === 'DRAFT' && this.hasPermission('EXPENSE_POST');
  }

  canCancel(expense: Expense): boolean {
    return expense.status === 'DRAFT' && this.hasPermission('EXPENSE_CANCEL');
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  statusClass(status?: ExpenseStatus): string {
    if (status === 'POSTED') return 'posted';
    if (status === 'CANCELLED') return 'cancelled';
    return 'draft';
  }

  trackExpense(index: number, expense: Expense): number {
    return expense.id || index;
  }

  private loadCategories(): void {
    this.expenseService.getCategories().subscribe({ next: categories => this.categories = categories });
  }
}
