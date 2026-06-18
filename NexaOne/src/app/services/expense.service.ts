import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Expense, ExpenseCategoryOption, ExpensePage, ExpensePaymentMethod, ExpenseStatus } from '../models/expense.model';
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

  getById(id: number): Observable<Expense> {
    return this.http.get<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  create(expense: Expense): Observable<Expense> {
    return this.http.post<Expense | ApiResponse<Expense>>(this.baseUrl, expense)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  update(id: number, expense: Expense): Observable<Expense> {
    return this.http.put<Expense | ApiResponse<Expense>>(`${this.baseUrl}/${id}`, expense)
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
}
