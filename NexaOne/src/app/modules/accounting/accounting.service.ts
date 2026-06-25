import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';
import { Account, AccountLedger, AccountingPeriod, AccountType, AccountingBook, BalanceSheet, Budget, BudgetActual, BudgetStatus, CostCenter, Expense, ExpenseCategory, FinancialDashboard, GeneralLedger, JournalEntry, JournalStatus, LedgerEntry, PageResult, PaymentMethod, ProfitLoss, TrialBalance, YearEndClosing } from './accounting.model';

@Injectable({ providedIn: 'root' })
export class AccountingService {
  private readonly baseUrl = `${environment.apiUrl}/accounting`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<ExpenseCategory[]> {
    return this.http.get<ExpenseCategory[] | ApiResponse<ExpenseCategory[]>>(`${this.baseUrl}/expense-categories`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getDeletedCategories(): Observable<ExpenseCategory[]> {
    return this.http.get<ExpenseCategory[] | ApiResponse<ExpenseCategory[]>>(`${this.baseUrl}/expense-categories/deleted`)
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

  restoreCategory(id: number): Observable<ExpenseCategory> {
    return this.http.put<ExpenseCategory | ApiResponse<ExpenseCategory>>(`${this.baseUrl}/expense-categories/${id}/restore`, {})
      .pipe(map(response => unwrapApiResponse(response)));
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

  getCostCenters(): Observable<CostCenter[]> { return this.http.get<CostCenter[] | ApiResponse<CostCenter[]>>(`${this.baseUrl}/cost-centers`).pipe(map(unwrapApiResponse)); }
  getCostCenterPage(filters: {keyword?:string;status?:string;page:number;size:number}): Observable<PageResult<CostCenter>> { let p=new HttpParams().set('page',filters.page).set('size',filters.size);if(filters.keyword)p=p.set('keyword',filters.keyword);if(filters.status)p=p.set('status',filters.status);return this.http.get<PageResult<CostCenter>|ApiResponse<PageResult<CostCenter>>>(`${this.baseUrl}/cost-centers/page`,{params:p}).pipe(map(unwrapApiResponse)); }
  saveCostCenter(value: CostCenter): Observable<CostCenter> { const r=value.id?this.http.put<CostCenter|ApiResponse<CostCenter>>(`${this.baseUrl}/cost-centers/${value.id}`,value):this.http.post<CostCenter|ApiResponse<CostCenter>>(`${this.baseUrl}/cost-centers`,value);return r.pipe(map(unwrapApiResponse)); }
  deactivateCostCenter(id:number):Observable<void>{return this.http.delete<void>(`${this.baseUrl}/cost-centers/${id}`);}
  getBudgetPage(filters:{keyword?:string;fiscalYear?:number|string;status?:BudgetStatus|string;page:number;size:number}):Observable<PageResult<Budget>>{let p=new HttpParams().set('page',filters.page).set('size',filters.size);if(filters.keyword)p=p.set('keyword',filters.keyword);if(filters.fiscalYear)p=p.set('fiscalYear',filters.fiscalYear);if(filters.status)p=p.set('status',filters.status);return this.http.get<PageResult<Budget>|ApiResponse<PageResult<Budget>>>(`${this.baseUrl}/budgets/page`,{params:p}).pipe(map(unwrapApiResponse));}
  getBudget(id:number):Observable<Budget>{return this.http.get<Budget|ApiResponse<Budget>>(`${this.baseUrl}/budgets/${id}`).pipe(map(unwrapApiResponse));}
  saveBudget(v:Budget):Observable<Budget>{const r=v.id?this.http.put<Budget|ApiResponse<Budget>>(`${this.baseUrl}/budgets/${v.id}`,v):this.http.post<Budget|ApiResponse<Budget>>(`${this.baseUrl}/budgets`,v);return r.pipe(map(unwrapApiResponse));}
  budgetAction(id:number,action:'approve'|'cancel'):Observable<Budget>{return this.http.post<Budget|ApiResponse<Budget>>(`${this.baseUrl}/budgets/${id}/${action}`,{}).pipe(map(unwrapApiResponse));}
  getBudgetActual(filters:Record<string,string|number|undefined>):Observable<BudgetActual>{let p=new HttpParams();Object.entries(filters).forEach(([k,v])=>{if(v!==undefined&&v!=='')p=p.set(k,String(v));});return this.http.get<BudgetActual|ApiResponse<BudgetActual>>(`${this.baseUrl}/budget-vs-actual`,{params:p}).pipe(map(unwrapApiResponse));}
  getPeriods():Observable<AccountingPeriod[]>{return this.http.get<AccountingPeriod[]|ApiResponse<AccountingPeriod[]>>(`${this.baseUrl}/periods`).pipe(map(unwrapApiResponse));}
  createPeriod(v:AccountingPeriod):Observable<AccountingPeriod>{return this.http.post<AccountingPeriod|ApiResponse<AccountingPeriod>>(`${this.baseUrl}/periods`,v).pipe(map(unwrapApiResponse));}
  periodAction(id:number,action:'close'|'reopen'):Observable<AccountingPeriod>{return this.http.post<AccountingPeriod|ApiResponse<AccountingPeriod>>(`${this.baseUrl}/periods/${id}/${action}`,{}).pipe(map(unwrapApiResponse));}
  getYearEnds():Observable<YearEndClosing[]>{return this.http.get<YearEndClosing[]|ApiResponse<YearEndClosing[]>>(`${this.baseUrl}/year-end-closings`).pipe(map(unwrapApiResponse));}
  prepareYearEnd(year:number):Observable<YearEndClosing>{return this.http.post<YearEndClosing|ApiResponse<YearEndClosing>>(`${this.baseUrl}/year-end-closings/prepare`,{},{params:new HttpParams().set('fiscalYear',year)}).pipe(map(unwrapApiResponse));}
  completeYearEnd(id:number):Observable<YearEndClosing>{return this.http.post<YearEndClosing|ApiResponse<YearEndClosing>>(`${this.baseUrl}/year-end-closings/${id}/complete`,{}).pipe(map(unwrapApiResponse));}
  getFinancialDashboard(year?:number):Observable<FinancialDashboard>{const params=year?new HttpParams().set('fiscalYear',year):undefined;return this.http.get<FinancialDashboard|ApiResponse<FinancialDashboard>>(`${this.baseUrl}/financial-dashboard`,{params}).pipe(map(unwrapApiResponse));}

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
