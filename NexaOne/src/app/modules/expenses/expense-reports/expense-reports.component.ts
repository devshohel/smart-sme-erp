import { Component, OnInit } from '@angular/core';
import { ExpenseCategoryOption, ExpenseReportRow, ExpenseStatus } from '../../../models/expense.model';
import { ExpenseService } from '../../../services/expense.service';
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

type ReportTab = 'summary' | 'category' | 'payment-method' | 'tax' | 'monthly';

@Component({
  selector: 'app-expense-reports',
  templateUrl: './expense-reports.component.html',
  styleUrls: ['./expense-reports.component.css']
})
export class ExpenseReportsComponent implements OnInit {
  rows: ExpenseReportRow[] = [];
  categories: ExpenseCategoryOption[] = [];
  activeTab: ReportTab = 'summary';
  loading = false;
  errorMessage = '';
  filters = {
    fromDate: '',
    toDate: '',
    categoryId: '' as number | '',
    status: '' as ExpenseStatus | ''
  };
  readonly tabs: { id: ReportTab; label: string }[] = [
    { id: 'summary', label: 'Summary' },
    { id: 'category', label: 'By Category' },
    { id: 'payment-method', label: 'By Payment Method' },
    { id: 'tax', label: 'Tax Report' },
    { id: 'monthly', label: 'Monthly Trend' }
  ];
  readonly statuses: ExpenseStatus[] = ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'POSTED', 'REVERSED', 'CANCELLED'];

  constructor(private expenseService: ExpenseService, private authService: AuthService) {}

  ngOnInit(): void {
    this.expenseService.getCategories().subscribe({ next: categories => this.categories = categories });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.expenseService.getReport(this.activeTab, this.filters).subscribe({
      next: rows => {
        this.rows = rows;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Expense report could not be loaded.');
      }
    });
  }

  selectTab(tab: ReportTab): void {
    this.activeTab = tab;
    this.load();
  }

  exportCsv(): void {
    this.download(this.csv(), `expense-${this.activeTab}.csv`, 'text/csv;charset=utf-8;');
  }

  exportExcel(): void {
    this.download(this.csv(), `expense-${this.activeTab}.xls`, 'application/vnd.ms-excel;charset=utf-8;');
  }

  print(): void {
    window.print();
  }

  canExport(): boolean {
    return this.authService.hasPermission('EXPENSE_REPORT_EXPORT');
  }

  total(field: keyof Pick<ExpenseReportRow, 'netAmount' | 'taxAmount' | 'grossAmount'>): number {
    return this.rows.reduce((sum, row) => sum + Number(row[field] || 0), 0);
  }

  private csv(): string {
    const data = [['Label', 'Count', 'Net Amount', 'Tax Amount', 'Gross Amount'], ...this.rows.map(row => [
      row.label, row.count, row.netAmount, row.taxAmount, row.grossAmount
    ])];
    return data.map(row => row.map(value => `"${String(value ?? '').replace(/"/g, '""')}"`).join(',')).join('\r\n');
  }

  private download(content: string, filename: string, type: string): void {
    const url = URL.createObjectURL(new Blob([content], { type }));
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
