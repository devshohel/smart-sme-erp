import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { Account, AccountType, BalanceSheet, BookEntry, Expense, ExpenseCategory, JournalEntry, JournalLine, JournalStatus, LedgerEntry, PaymentMethod, TrialBalance } from '../accounting.model';
import { AccountingService } from '../accounting.service';

type Section = 'categories' | 'expenses' | 'accounts' | 'journals' | 'cash-book' | 'bank-book' | 'customer-ledger' | 'supplier-ledger' | 'general-ledger' | 'trial-balance' | 'balance-sheet';

@Component({
  selector: 'app-accounting',
  templateUrl: './accounting.component.html',
  styleUrls: ['./accounting.component.css']
})
export class AccountingComponent implements OnInit {
  section: Section = 'expenses';
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  categories: ExpenseCategory[] = [];
  expenses: Expense[] = [];
  accounts: Account[] = [];
  journals: JournalEntry[] = [];
  bookEntries: BookEntry[] = [];
  ledgerEntries: LedgerEntry[] = [];
  trialBalance: TrialBalance | null = null;
  balanceSheet: BalanceSheet | null = null;

  categoryForm: ExpenseCategory = this.emptyCategory();
  expenseForm: Expense = this.emptyExpense();
  accountForm: Account = this.emptyAccount();
  journalForm: JournalEntry = this.emptyJournal();

  expenseFilters = { fromDate: '', toDate: '', categoryId: '' as number | '', paymentMethod: '' as PaymentMethod | '' };
  ledgerFilters = { customerId: '' as number | '', supplierId: '' as number | '', accountId: '' as number | '', fromDate: '', toDate: '' };
  accountTypeFilter: AccountType | '' = '';
  journalStatusFilter: JournalStatus | '' = '';

  readonly paymentMethods: PaymentMethod[] = ['CASH', 'BANK', 'OTHER'];
  readonly accountTypes: AccountType[] = ['ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE'];
  readonly journalStatuses: JournalStatus[] = ['DRAFT', 'POSTED', 'CANCELLED'];

  constructor(
    private route: ActivatedRoute,
    private accountingService: AccountingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] || 'expenses';
      this.clearMessages();
      this.loadReferenceData();
      this.loadCurrentSection();
    });
  }

  loadCurrentSection(): void {
    if (this.section === 'categories') this.loadCategories();
    if (this.section === 'expenses') this.loadExpenses();
    if (this.section === 'accounts') this.loadAccounts();
    if (this.section === 'journals') this.loadJournals();
    if (this.section === 'cash-book') this.loadBook(true);
    if (this.section === 'bank-book') this.loadBook(false);
    if (this.section === 'customer-ledger') this.loadCustomerLedger();
    if (this.section === 'supplier-ledger') this.loadSupplierLedger();
    if (this.section === 'general-ledger') this.loadGeneralLedger();
    if (this.section === 'trial-balance') this.loadTrialBalance();
    if (this.section === 'balance-sheet') this.loadBalanceSheet();
  }

  saveCategory(): void {
    this.save(this.accountingService.saveCategory(this.categoryForm), 'Category saved.', () => {
      this.categoryForm = this.emptyCategory();
      this.loadCategories();
    });
  }

  editCategory(category: ExpenseCategory): void {
    this.categoryForm = { ...category };
  }

  deactivateCategory(category: ExpenseCategory): void {
    if (!category.id || !confirm(`Deactivate category "${category.name}"?`)) return;
    this.save(this.accountingService.deactivateCategory(category.id), 'Category deactivated.', () => this.loadCategories());
  }

  saveExpense(): void {
    this.save(this.accountingService.saveExpense(this.expenseForm), 'Expense saved.', () => {
      this.expenseForm = this.emptyExpense();
      this.loadExpenses();
    });
  }

  editExpense(expense: Expense): void {
    this.expenseForm = { ...expense };
  }

  cancelExpense(expense: Expense): void {
    if (!expense.id || !confirm(`Cancel expense "${expense.expenseNo}"?`)) return;
    this.save(this.accountingService.cancelExpense(expense.id), 'Expense cancelled.', () => this.loadExpenses());
  }

  saveAccount(): void {
    this.save(this.accountingService.saveAccount(this.accountForm), 'Account saved.', () => {
      this.accountForm = this.emptyAccount();
      this.loadAccounts();
    });
  }

  editAccount(account: Account): void {
    this.accountForm = { ...account };
  }

  saveJournal(): void {
    this.save(this.accountingService.saveJournal(this.journalForm), 'Journal entry saved as draft.', () => {
      this.journalForm = this.emptyJournal();
      this.loadJournals();
    });
  }

  postJournal(journal: JournalEntry): void {
    if (!journal.id || !confirm(`Post journal "${journal.journalNo}"?`)) return;
    this.save(this.accountingService.postJournal(journal.id), 'Journal posted.', () => {
      this.loadJournals();
      if (this.section === 'cash-book' || this.section === 'bank-book') this.loadCurrentSection();
    });
  }

  cancelJournal(journal: JournalEntry): void {
    if (!journal.id || !confirm(`Cancel journal "${journal.journalNo}"?`)) return;
    this.save(this.accountingService.cancelJournal(journal.id), 'Journal cancelled.', () => this.loadJournals());
  }

  addJournalLine(): void {
    this.journalForm.lines.push(this.emptyLine());
  }

  removeJournalLine(index: number): void {
    if (this.journalForm.lines.length > 2) {
      this.journalForm.lines.splice(index, 1);
    }
  }

  totalDebit(): number {
    return this.journalForm.lines.reduce((sum, line) => sum + this.toNumber(line.debit), 0);
  }

  totalCredit(): number {
    return this.journalForm.lines.reduce((sum, line) => sum + this.toNumber(line.credit), 0);
  }

  expenseTotal(): number {
    return this.expenses.filter(expense => expense.status !== 'CANCELLED').reduce((sum, expense) => sum + this.toNumber(expense.amount), 0);
  }

  bookInTotal(): number {
    return this.bookEntries.reduce((sum, row) => sum + this.toNumber(row.moneyIn), 0);
  }

  bookOutTotal(): number {
    return this.bookEntries.reduce((sum, row) => sum + this.toNumber(row.moneyOut), 0);
  }

  finalBalance(): number {
    return this.bookEntries.length ? this.bookEntries[this.bookEntries.length - 1].balance : 0;
  }

  ledgerDebitTotal(): number {
    return this.ledgerEntries.reduce((sum, row) => sum + this.toNumber(row.debit), 0);
  }

  ledgerCreditTotal(): number {
    return this.ledgerEntries.reduce((sum, row) => sum + this.toNumber(row.credit), 0);
  }

  ledgerBalance(): number {
    return this.ledgerEntries.length ? this.ledgerEntries[this.ledgerEntries.length - 1].balance : 0;
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  badgeClass(status?: string): string {
    if (status === 'ACTIVE' || status === 'POSTED') return 'bg-success-subtle text-success';
    if (status === 'DRAFT') return 'bg-warning-subtle text-warning';
    if (status === 'CANCELLED' || status === 'INACTIVE') return 'bg-secondary-subtle text-secondary';
    return 'bg-light text-dark';
  }

  private loadReferenceData(): void {
    if (!this.categories.length) this.loadCategories(false);
    if (!this.accounts.length) this.loadAccounts(false);
  }

  private loadCategories(showLoading = true): void {
    this.fetch(showLoading, this.accountingService.getCategories(), categories => this.categories = categories, 'Categories could not be loaded.');
  }

  private loadExpenses(): void {
    this.fetch(true, this.accountingService.getExpenses(this.expenseFilters), expenses => this.expenses = expenses, 'Expenses could not be loaded.');
  }

  private loadAccounts(showLoading = true): void {
    this.fetch(showLoading, this.accountingService.getAccounts(this.accountTypeFilter), accounts => this.accounts = accounts, 'Accounts could not be loaded.');
  }

  private loadJournals(): void {
    this.fetch(true, this.accountingService.getJournals(this.journalStatusFilter), journals => this.journals = journals, 'Journal entries could not be loaded.');
  }

  private loadBook(cash: boolean): void {
    this.fetch(true, cash ? this.accountingService.getCashBook() : this.accountingService.getBankBook(), rows => this.bookEntries = rows, 'Book entries could not be loaded.');
  }

  private loadCustomerLedger(): void {
    this.fetch(true, this.accountingService.getCustomerLedger(this.ledgerFilters), rows => this.ledgerEntries = rows, 'Customer ledger could not be loaded.');
  }

  private loadSupplierLedger(): void {
    this.fetch(true, this.accountingService.getSupplierLedger(this.ledgerFilters), rows => this.ledgerEntries = rows, 'Supplier ledger could not be loaded.');
  }

  private loadGeneralLedger(): void {
    this.fetch(true, this.accountingService.getGeneralLedger(this.ledgerFilters), rows => this.ledgerEntries = rows, 'General ledger could not be loaded.');
  }

  private loadTrialBalance(): void {
    this.fetch(true, this.accountingService.getTrialBalance(this.ledgerFilters), report => this.trialBalance = report, 'Trial balance could not be loaded.');
  }

  private loadBalanceSheet(): void {
    this.fetch(true, this.accountingService.getBalanceSheet(), report => this.balanceSheet = report, 'Balance sheet could not be loaded.');
  }

  private fetch<T>(showLoading: boolean, request: Observable<T>, next: (value: T) => void, fallback: string): void {
    if (showLoading) this.loading = true;
    this.errorMessage = '';
    request.subscribe({
      next: (value: T) => {
        next(value);
        this.loading = false;
      },
      error: (error: unknown) => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, fallback);
        debugApiError('AccountingComponent.fetch', error);
      }
    });
  }

  private save<T>(request: Observable<T>, message: string, afterSave: () => void): void {
    this.saving = true;
    this.clearMessages();
    request.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = message;
        afterSave();
      },
      error: (error: unknown) => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Save request failed.');
        debugApiError('AccountingComponent.save', error);
      }
    });
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private emptyCategory(): ExpenseCategory {
    return { name: '', description: '', status: 'ACTIVE' };
  }

  private emptyExpense(): Expense {
    return { expenseDate: new Date().toISOString().slice(0, 10), categoryId: null, amount: 0, paymentMethod: 'CASH', referenceNo: '', notes: '', status: 'ACTIVE' };
  }

  private emptyAccount(): Account {
    return { accountCode: '', accountName: '', accountType: 'ASSET', parentAccountId: null, status: 'ACTIVE' };
  }

  private emptyJournal(): JournalEntry {
    return { journalDate: new Date().toISOString().slice(0, 10), referenceNo: '', description: '', lines: [this.emptyLine(), this.emptyLine()] };
  }

  private emptyLine(): JournalLine {
    return { accountId: null, debit: 0, credit: 0, description: '' };
  }

  private toNumber(value: number | undefined | null): number {
    return Number(value || 0);
  }
}
