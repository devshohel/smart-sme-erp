import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Expense, ExpenseCategoryOption, ExpensePage, ExpensePaymentMethod, ExpenseReportRow, ExpenseStatus } from '../models/expense.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({ providedIn: 'root' })
export class ExpenseService {
  private readonly baseUrl = `${environment.apiUrl}/expenses`;
  private readonly categoryUrl = `${environment.apiUrl}/accounting/expense-categories`;

  constructor(private http: HttpClient) {}

  getPage(filters: {
    keyword?: string;
    categoryId?: number | '';
    paymentMethod?: ExpensePaymentMethod | '';
    status?: ExpenseStatus | '';
    fromDate?: string;
    toDate?: string;
    page?: number;
    size?: number;
    sort?: string;
    direction?: 'asc' | 'desc';
  }): Observable<ExpensePage> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 10))
      .set('sort', filters.sort || 'expenseDate')
      .set('direction', filters.direction || 'desc');

    Object.entries(filters).forEach(([key, value]) => {
      if (!['page', 'size', 'sort', 'direction'].includes(key) && value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });

    return this.http.get<ExpensePage | ApiResponse<ExpensePage>>(`${this.baseUrl}/page`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getApprovalQueue(filters: {
    fromDate?: string;
    toDate?: string;
    categoryId?: number | '';
    submittedBy?: string;
    amountMin?: number | '';
    amountMax?: number | '';
  }): Observable<Expense[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<Expense[] | ApiResponse<Expense[]>>(`${this.baseUrl}/approval-queue`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getById(id: number): Observable<Expense> {
    return this.http.get<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  create(expense: Expense, receipt?: File | null): Observable<Expense> {
    const body = receipt ? this.toFormData(expense, receipt) : expense;
    return this.http.post<Expense | ApiResponse<Expense>>(this.baseUrl, body)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  update(id: number, expense: Expense, receipt?: File | null): Observable<Expense> {
    const body = receipt ? this.toFormData(expense, receipt) : expense;
    return this.http.put<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}`, body)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  submit(id: number): Observable<Expense> {
    return this.http.post<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}/submit`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  approve(id: number): Observable<Expense> {
    return this.http.post<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}/approve`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  reject(id: number, reason: string): Observable<Expense> {
    return this.http.post<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}/reject`, { reason })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  reverse(id: number, reversalReason: string): Observable<Expense> {
    return this.http.post<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}/reverse`, { reversalReason })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  post(id: number): Observable<Expense> {
    return this.http.post<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}/post`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  cancel(id: number): Observable<Expense> {
    return this.http.delete<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCategories(): Observable<ExpenseCategoryOption[]> {
    return this.http.get<ExpenseCategoryOption[] | ApiResponse<ExpenseCategoryOption[]>>(this.categoryUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getReport(type: 'summary' | 'category' | 'payment-method' | 'tax' | 'monthly', filters: {
    fromDate?: string;
    toDate?: string;
    categoryId?: number | '';
    status?: ExpenseStatus | '';
  }): Observable<ExpenseReportRow[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<ExpenseReportRow[] | ApiResponse<ExpenseReportRow[]>>(`${this.baseUrl}/reports/${type}`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  private toFormData(expense: Expense, receipt: File): FormData {
    const formData = new FormData();
    formData.append('expense', new Blob([JSON.stringify(expense)], { type: 'application/json' }));
    formData.append('receipt', receipt);
    return formData;
  }
}
