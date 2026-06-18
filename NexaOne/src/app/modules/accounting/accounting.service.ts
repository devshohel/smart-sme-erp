import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';
import { Account, AccountLedger, AccountType, AccountingBook, BalanceSheet, Expense, ExpenseCategory, GeneralLedger, JournalEntry, JournalStatus, LedgerEntry, PaymentMethod, ProfitLoss, TrialBalance } from './accounting.model';

@Injectable({ providedIn: 'root' })
export class AccountingService {
  private readonly baseUrl = `${environment.apiUrl}/accounting`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<ExpenseCategory[]> {
    return this.http.get<ExpenseCategory[] | ApiResponse<ExpenseCategory[]>>(`${this.baseUrl}/expense-categories`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveCategory(category: ExpenseCategory): Observable<ExpenseCategory> {
    const request$ = category.id
      ? this.http.put<ExpenseCategory | ApiResponse<ExpenseCategory>>(`${this.baseUrl}/expense-categories/${category.id}`, category)
      : this.http.post<ExpenseCategory | ApiResponse<ExpenseCategory>>(`${this.baseUrl}/expense-categories`, category);
    return request$.pipe(map(response => unwrapApiResponse(response)));
  }

  deactivateCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/expense-categories/${id}`);
  }

  getExpenses(filters: { fromDate?: string; toDate?: string; categoryId?: number | ''; paymentMethod?: PaymentMethod | '' }): Observable<Expense[]> {
    let params = new HttpParams();
    if (filters.fromDate) params = params.set('fromDate', filters.fromDate);
    if (filters.toDate) params = params.set('toDate', filters.toDate);
    if (filters.categoryId) params = params.set('categoryId', String(filters.categoryId));
    if (filters.paymentMethod) params = params.set('paymentMethod', filters.paymentMethod);
    return this.http.get<Expense[] | ApiResponse<Expense[]>>(`${this.baseUrl}/expenses`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveExpense(expense: Expense): Observable<Expense> {
    const request$ = expense.id
      ? this.http.put<Expense | ApiResponse<Expense>>(`${this.baseUrl}/expenses/${expense.id}`, expense)
      : this.http.post<Expense | ApiResponse<Expense>>(`${this.baseUrl}/expenses`, expense);
    return request$.pipe(map(response => unwrapApiResponse(response)));
  }

  cancelExpense(id: number): Observable<Expense> {
    return this.http.delete<Expense | ApiResponse<Expense>>(`${this.baseUrl}/expenses/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getAccounts(type?: AccountType | ''): Observable<Account[]> {
    const params = type ? new HttpParams().set('type', type) : undefined;
    return this.http.get<Account[] | ApiResponse<Account[]>>(`${this.baseUrl}/accounts`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveAccount(account: Account): Observable<Account> {
    const request$ = account.id
      ? this.http.put<Account | ApiResponse<Account>>(`${this.baseUrl}/accounts/${account.id}`, account)
      : this.http.post<Account | ApiResponse<Account>>(`${this.baseUrl}/accounts`, account);
    return request$.pipe(map(response => unwrapApiResponse(response)));
  }

  getJournals(status?: JournalStatus | ''): Observable<JournalEntry[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<JournalEntry[] | ApiResponse<JournalEntry[]>>(`${this.baseUrl}/journal-entries`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveJournal(entry: JournalEntry): Observable<JournalEntry> {
    const request$ = entry.id
      ? this.http.put<JournalEntry | ApiResponse<JournalEntry>>(`${this.baseUrl}/journal-entries/${entry.id}`, entry)
      : this.http.post<JournalEntry | ApiResponse<JournalEntry>>(`${this.baseUrl}/journal-entries`, entry);
    return request$
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getJournal(id: number): Observable<JournalEntry> {
    return this.http.get<JournalEntry | ApiResponse<JournalEntry>>(`${this.baseUrl}/journal-entries/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  postJournal(id: number): Observable<JournalEntry> {
    return this.http.post<JournalEntry | ApiResponse<JournalEntry>>(`${this.baseUrl}/journal-entries/${id}/post`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  cancelJournal(id: number): Observable<JournalEntry> {
    return this.http.post<JournalEntry | ApiResponse<JournalEntry>>(`${this.baseUrl}/journal-entries/${id}/cancel`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCashBook(filters: { fromDate?: string; toDate?: string }): Observable<AccountingBook> {
    return this.getBook(`${this.baseUrl}/cash-book`, filters);
  }

  getBankBook(filters: { fromDate?: string; toDate?: string }): Observable<AccountingBook> {
    return this.getBook(`${this.baseUrl}/bank-book`, filters);
  }

  private getBook(url: string, filters: { fromDate?: string; toDate?: string }): Observable<AccountingBook> {
    let params = new HttpParams();
    if (filters.fromDate) params = params.set('fromDate', filters.fromDate);
    if (filters.toDate) params = params.set('toDate', filters.toDate);
    return this.http.get<AccountingBook | ApiResponse<AccountingBook>>(url, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCustomerLedger(filters: { customerId?: number | ''; fromDate?: string; toDate?: string }): Observable<LedgerEntry[]> {
    return this.getLedger(`${this.baseUrl}/customer-ledger`, filters, 'customerId');
  }

  getSupplierLedger(filters: { supplierId?: number | ''; fromDate?: string; toDate?: string }): Observable<LedgerEntry[]> {
    return this.getLedger(`${this.baseUrl}/supplier-ledger`, filters, 'supplierId');
  }

  getGeneralLedger(filters: { fromDate?: string; toDate?: string }): Observable<GeneralLedger> {
    return this.getReport<GeneralLedger>(`${this.baseUrl}/general-ledger`, filters);
  }

  getAccountLedger(accountId: number, filters: { fromDate?: string; toDate?: string }): Observable<AccountLedger> {
    return this.getReport<AccountLedger>(`${this.baseUrl}/account-ledger/${accountId}`, filters);
  }

  getProfitLoss(filters: { fromDate?: string; toDate?: string }): Observable<ProfitLoss> {
    return this.getReport<ProfitLoss>(`${this.baseUrl}/profit-loss`, filters);
  }

  getTrialBalance(filters: { asOfDate?: string }): Observable<TrialBalance> {
    let params = new HttpParams();
    if (filters.asOfDate) params = params.set('asOfDate', filters.asOfDate);
    return this.http.get<TrialBalance | ApiResponse<TrialBalance>>(`${this.baseUrl}/trial-balance`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getBalanceSheet(asOfDate?: string): Observable<BalanceSheet> {
    const params = asOfDate ? new HttpParams().set('asOfDate', asOfDate) : undefined;
    return this.http.get<BalanceSheet | ApiResponse<BalanceSheet>>(`${this.baseUrl}/balance-sheet`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  private getReport<T>(url: string, filters: { fromDate?: string; toDate?: string }): Observable<T> {
    let params = new HttpParams();
    if (filters.fromDate) params = params.set('fromDate', filters.fromDate);
    if (filters.toDate) params = params.set('toDate', filters.toDate);
    return this.http.get<T | ApiResponse<T>>(url, { params }).pipe(map(response => unwrapApiResponse(response)));
  }

  private getLedger(url: string, filters: Record<string, number | string | undefined>, idKey: string): Observable<LedgerEntry[]> {
    let params = new HttpParams();
    if (filters[idKey]) params = params.set(idKey, String(filters[idKey]));
    if (filters['fromDate']) params = params.set('fromDate', String(filters['fromDate']));
    if (filters['toDate']) params = params.set('toDate', String(filters['toDate']));
    return this.http.get<LedgerEntry[] | ApiResponse<LedgerEntry[]>>(url, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
