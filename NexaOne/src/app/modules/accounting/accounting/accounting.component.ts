import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { Account, AccountLedger, AccountingPeriod, AccountType, BalanceSheet, BookEntry, Budget, BudgetActual, BudgetStatus, CostCenter, Expense, ExpenseCategory, FinancialDashboard, GeneralLedger, GeneralLedgerRow, JournalEntry, JournalLine, JournalStatus, LedgerEntry, PaymentMethod, ProfitLoss, TrialBalance, YearEndClosing } from '../accounting.model';
import { AccountingService } from '../accounting.service';

type Section = 'categories' | 'expenses' | 'accounts' | 'journals' | 'cash-book' | 'bank-book' | 'customer-ledger' | 'supplier-ledger' | 'general-ledger' | 'account-ledger' | 'profit-loss' | 'trial-balance' | 'balance-sheet' | 'cost-centers' | 'budgets' | 'budget-vs-actual' | 'financial-dashboard' | 'periods' | 'year-end-closings';

@Component({
  selector: 'app-accounting',
  templateUrl: './accounting.component.html',
  styleUrls: ['./accounting.component.css']
})
export class AccountingComponent implements OnInit {
  section: Section = 'expenses';
  journalMode: 'list' | 'create' | 'edit' | 'details' = 'list';
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
  generalLedger: GeneralLedger | null = null;
  accountLedger: AccountLedger | null = null;
  profitLoss: ProfitLoss | null = null;
  balanceSheet: BalanceSheet | null = null;
  costCenters: CostCenter[] = [];
  costCenterForm: CostCenter = { code: '', name: '', description: '', status: 'ACTIVE' };
  costCenterFilters = { keyword: '', status: '' };
  costCenterPage = 0; costCenterTotalPages = 1;
  budgets: Budget[] = []; budgetMode: 'list'|'create'|'edit'|'details' = 'list'; budgetPage=0; budgetTotalPages=1;
  budgetFilters = { keyword:'', fiscalYear:'' as number|'', status:'' as BudgetStatus|'' };
  budgetForm: Budget = this.emptyBudget();
  budgetActual: BudgetActual | null = null;
  budgetActualFilters = { fiscalYear: new Date().getFullYear(), fromDate:'', toDate:'', accountId:'' as number|'', costCenterId:'' as number|'' };
  financialDashboard: FinancialDashboard | null = null; dashboardYear = new Date().getFullYear();
  periods: AccountingPeriod[]=[]; periodForm: AccountingPeriod={periodName:'',startDate:'',endDate:'',status:'OPEN',remarks:''};
  yearEnds: YearEndClosing[]=[]; yearEndYear=new Date().getFullYear();

  categoryForm: ExpenseCategory = this.emptyCategory();
  expenseForm: Expense = this.emptyExpense();
  accountForm: Account = this.emptyAccount();
  journalForm: JournalEntry = this.emptyJournal();

  expenseFilters = { fromDate: '', toDate: '', categoryId: '' as number | '', paymentMethod: '' as PaymentMethod | '' };
  ledgerFilters = { customerId: '' as number | '', supplierId: '' as number | '', accountId: '' as number | '', fromDate: '', toDate: '' };
  accountTypeFilter: AccountType | '' = '';
  journalStatusFilter: JournalStatus | '' = '';
  journalSearch = '';
  journalFromDate = '';
  journalToDate = '';
  journalPage = 1;
  readonly journalPageSize = 10;
  bookFilters = { fromDate: '', toDate: '' };
  openingBalance = 0;
  closingBalance = 0;
  trialBalanceAsOfDate = '';
  financialFilters = { fromDate: '', toDate: '' };
  balanceSheetAsOfDate = '';
  expandedAccountTypes: Record<AccountType, boolean> = { ASSET: true, LIABILITY: true, EQUITY: true, INCOME: true, EXPENSE: true };
  expandedStatements = { assets: true, liabilities: true, equity: true, income: true, expenses: true };

  readonly paymentMethods: PaymentMethod[] = ['CASH', 'BANK', 'MOBILE_BANKING', 'OTHER'];
  readonly accountTypes: AccountType[] = ['ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE'];
  readonly journalStatuses: JournalStatus[] = ['DRAFT', 'POSTED', 'CANCELLED'];
  readonly Math = Math;

  constructor(
    private route: ActivatedRoute,
    private accountingService: AccountingService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.section = data['section'] || 'expenses';
      this.journalMode = data['mode'] || 'list';
      this.budgetMode = data['mode'] || 'list';
      this.clearMessages();
      this.loadReferenceData();
      const journalId = Number(this.route.snapshot.paramMap.get('id'));
      if (this.section === 'journals' && journalId) this.loadJournal(journalId);
      if (this.section === 'budgets' && journalId) this.loadBudget(journalId);
      this.loadCurrentSection();
    });
  }

  loadCurrentSection(): void {
    if (this.section === 'categories') this.loadCategories();
    if (this.section === 'expenses') this.loadExpenses();
    if (this.section === 'accounts') this.loadAccounts();
    if (this.section === 'journals' && this.journalMode === 'list') this.loadJournals();
    if (this.section === 'cash-book') this.loadBook(true);
    if (this.section === 'bank-book') this.loadBook(false);
    if (this.section === 'customer-ledger') this.loadCustomerLedger();
    if (this.section === 'supplier-ledger') this.loadSupplierLedger();
    if (this.section === 'general-ledger') this.loadGeneralLedger();
    if (this.section === 'account-ledger') this.loadAccountLedger();
    if (this.section === 'profit-loss') this.loadProfitLoss();
    if (this.section === 'trial-balance') this.loadTrialBalance();
    if (this.section === 'balance-sheet') this.loadBalanceSheet();
    if (this.section === 'cost-centers') this.loadCostCentersPage();
    if (this.section === 'budgets' && this.budgetMode === 'list') this.loadBudgets();
    if (this.section === 'budget-vs-actual') this.loadBudgetActual();
    if (this.section === 'financial-dashboard') this.loadFinancialDashboard();
    if (this.section === 'periods') this.loadPeriods();
    if (this.section === 'year-end-closings') this.loadYearEnds();
  }

  saveCostCenter():void{this.save(this.accountingService.saveCostCenter(this.costCenterForm),'Cost center saved.',()=>{this.costCenterForm={code:'',name:'',description:'',status:'ACTIVE'};this.loadCostCentersPage();this.loadCostCenterOptions();});}
  editCostCenter(v:CostCenter):void{this.costCenterForm={...v};}
  deactivateCostCenter(v:CostCenter):void{if(v.id&&confirm(`Deactivate ${v.code}?`))this.save(this.accountingService.deactivateCostCenter(v.id),'Cost center deactivated.',()=>this.loadCostCentersPage());}
  saveBudget():void{this.save(this.accountingService.saveBudget(this.budgetForm),'Budget saved.',()=>this.router.navigate(['/accounting/budgets']));}
  budgetAction(v:Budget,action:'approve'|'cancel'):void{if(v.id&&confirm(`${action} ${v.budgetNo}?`))this.save(this.accountingService.budgetAction(v.id,action),`Budget ${action}d.`,()=>this.loadBudgets());}
  createPeriod():void{this.save(this.accountingService.createPeriod(this.periodForm),'Accounting period created.',()=>{this.periodForm={periodName:'',startDate:'',endDate:'',status:'OPEN',remarks:''};this.loadPeriods();});}
  periodAction(v:AccountingPeriod,action:'close'|'reopen'):void{if(v.id&&confirm(`${action} ${v.periodName}?`))this.save(this.accountingService.periodAction(v.id,action),`Period ${action}d.`,()=>this.loadPeriods());}
  prepareYearEnd():void{this.save(this.accountingService.prepareYearEnd(this.yearEndYear),'Year-end closing prepared.',()=>this.loadYearEnds());}
  completeYearEnd(v:YearEndClosing):void{if(v.id&&confirm(`Complete closing for ${v.fiscalYear}?`))this.save(this.accountingService.completeYearEnd(v.id),'Year-end closing completed.',()=>this.loadYearEnds());}

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
      this.router.navigate(['/accounting/journals']);
    });
  }

  saveAndPostJournal(): void {
    this.saving = true;
    this.clearMessages();
    this.accountingService.saveJournal(this.journalForm).subscribe({
      next: saved => {
        if (!saved.id) return;
        this.accountingService.postJournal(saved.id).subscribe({
          next: () => { this.saving = false; this.router.navigate(['/accounting/journals']); },
          error: error => { this.saving = false; this.errorMessage = extractApiErrorMessage(error, 'Journal could not be posted.'); }
        });
      },
      error: error => { this.saving = false; this.errorMessage = extractApiErrorMessage(error, 'Journal could not be saved.'); }
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
    return this.expenses.filter(expense => expense.status === 'POSTED').reduce((sum, expense) => sum + this.toNumber(expense.amount), 0);
  }

  bookInTotal(): number {
    return this.bookEntries.reduce((sum, row) => sum + this.toNumber(row.debit), 0);
  }

  bookOutTotal(): number {
    return this.bookEntries.reduce((sum, row) => sum + this.toNumber(row.credit), 0);
  }

  finalBalance(): number {
    return this.closingBalance;
  }

  filteredJournals(): JournalEntry[] {
    const query = this.journalSearch.trim().toLowerCase();
    return this.journals.filter(journal =>
      (!query || [journal.journalNo, journal.referenceNo, journal.description].some(value => value?.toLowerCase().includes(query))) &&
      (!this.journalFromDate || journal.journalDate >= this.journalFromDate) &&
      (!this.journalToDate || journal.journalDate <= this.journalToDate));
  }

  pagedJournals(): JournalEntry[] {
    const start = (this.journalPage - 1) * this.journalPageSize;
    return this.filteredJournals().slice(start, start + this.journalPageSize);
  }

  journalPageCount(): number { return Math.max(1, Math.ceil(this.filteredJournals().length / this.journalPageSize)); }
  journalBalanced(): boolean { return Math.abs(this.totalDebit() - this.totalCredit()) < 0.005 && this.totalDebit() > 0; }

  exportCurrentReport(format: 'csv' | 'xls' = 'csv'): void {
    let rows: Array<Array<string | number>>;
    if (this.section === 'trial-balance') rows = [['Account Code', 'Account Name', 'Account Type', 'Debit Balance', 'Credit Balance'], ...(this.trialBalance?.rows || []).map(r => [r.accountCode, r.accountName, r.accountType, r.debitBalance, r.creditBalance])];
    else if (this.section === 'general-ledger') rows = [['Account Code', 'Account Name', 'Type', 'Opening', 'Debit', 'Credit', 'Closing'], ...(this.generalLedger?.accounts || []).map(r => [r.accountCode, r.accountName, r.accountType, r.openingBalance, r.totalDebit, r.totalCredit, r.closingBalance])];
    else if (this.section === 'account-ledger') rows = [['Date', 'Journal No', 'Reference Type', 'Reference No', 'Description', 'Debit', 'Credit', 'Running Balance'], ...(this.accountLedger?.transactions || []).map(r => [r.date, r.journalNo, r.referenceType || '', r.referenceNo || '', r.description || '', r.debit, r.credit, r.runningBalance])];
    else if (this.section === 'profit-loss') rows = [['Section', 'Group', 'Account Code', 'Account Name', 'Amount'], ...(this.profitLoss?.income || []).map(r => ['Income', r.groupName, r.accountCode, r.accountName, r.amount]), ...(this.profitLoss?.expenses || []).map(r => ['Expense', r.groupName, r.accountCode, r.accountName, r.amount])];
    else if (this.section === 'balance-sheet') rows = [['Section', 'Group', 'Account Code', 'Account Name', 'Amount'], ...(this.balanceSheet?.assets || []).map(r => ['Assets', r.groupName, r.accountCode, r.accountName, r.amount]), ...(this.balanceSheet?.liabilities || []).map(r => ['Liabilities', r.groupName, r.accountCode, r.accountName, r.amount]), ...(this.balanceSheet?.equity || []).map(r => ['Equity', r.groupName, r.accountCode, r.accountName, r.amount]), ['Equity', 'Current Profit/Loss', '', '', this.balanceSheet?.currentProfitLoss || 0]];
    else if (this.section === 'budget-vs-actual') rows = [['Account','Cost Center','Budget','Actual','Variance','Variance %'],...(this.budgetActual?.rows||[]).map(r=>[`${r.accountCode} - ${r.accountName}`,r.costCenterName,r.budgetAmount,r.actualAmount,r.variance,r.variancePercentage])];
    else rows = [['Date', 'Journal No', 'Reference Type', 'Reference No', 'Description', 'Debit', 'Credit', 'Running Balance'], ...this.bookEntries.map(r => [r.date, r.journalNo, r.referenceType || '', r.referenceNo || '', r.description || '', r.debit, r.credit, r.runningBalance])];
    const csv = rows.map(row => row.map(value => `"${String(value ?? '').replace(/"/g, '""')}"`).join(',')).join('\n');
    const link = document.createElement('a');
    link.href = URL.createObjectURL(new Blob([csv], { type: format === 'xls' ? 'application/vnd.ms-excel;charset=utf-8;' : 'text/csv;charset=utf-8;' }));
    link.download = `${this.section}.${format}`;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  exportExcel(): void { this.exportCurrentReport('xls'); }

  print(): void { window.print(); }

  generalLedgerByType(type: AccountType): GeneralLedgerRow[] {
    return (this.generalLedger?.accounts || []).filter(account => account.accountType === type);
  }

  toggleAccountType(type: AccountType): void { this.expandedAccountTypes[type] = !this.expandedAccountTypes[type]; }

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
    if (!this.costCenters.length && this.hasPermission('COST_CENTER_VIEW')) this.loadCostCenterOptions();
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

  private loadJournal(id: number): void {
    this.fetch(true, this.accountingService.getJournal(id), journal => this.journalForm = journal, 'Journal entry could not be loaded.');
  }

  private loadBook(cash: boolean): void {
    this.fetch(true, cash ? this.accountingService.getCashBook(this.bookFilters) : this.accountingService.getBankBook(this.bookFilters), report => {
      this.bookEntries = report.rows;
      this.openingBalance = report.openingBalance;
      this.closingBalance = report.closingBalance;
    }, 'Book entries could not be loaded.');
  }

  private loadCustomerLedger(): void {
    this.fetch(true, this.accountingService.getCustomerLedger(this.ledgerFilters), rows => this.ledgerEntries = rows, 'Customer ledger could not be loaded.');
  }

  private loadSupplierLedger(): void {
    this.fetch(true, this.accountingService.getSupplierLedger(this.ledgerFilters), rows => this.ledgerEntries = rows, 'Supplier ledger could not be loaded.');
  }

  private loadGeneralLedger(): void {
    this.fetch(true, this.accountingService.getGeneralLedger(this.financialFilters), report => this.generalLedger = report, 'General ledger could not be loaded.');
  }

  private loadAccountLedger(): void {
    const accountId = Number(this.route.snapshot.paramMap.get('id'));
    if (!accountId) return;
    this.fetch(true, this.accountingService.getAccountLedger(accountId, this.financialFilters), report => this.accountLedger = report, 'Account ledger could not be loaded.');
  }

  private loadProfitLoss(): void {
    this.fetch(true, this.accountingService.getProfitLoss(this.financialFilters), report => this.profitLoss = report, 'Profit and loss statement could not be loaded.');
  }

  private loadTrialBalance(): void {
    this.fetch(true, this.accountingService.getTrialBalance({ asOfDate: this.trialBalanceAsOfDate }), report => this.trialBalance = report, 'Trial balance could not be loaded.');
  }

  private loadBalanceSheet(): void {
    this.fetch(true, this.accountingService.getBalanceSheet(this.balanceSheetAsOfDate), report => this.balanceSheet = report, 'Balance sheet could not be loaded.');
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
    return { name: '', description: '', accountId: null, status: 'ACTIVE' };
  }

  private loadCostCenterOptions():void{this.fetch(false,this.accountingService.getCostCenters(),rows=>this.costCenters=rows,'Cost centers could not be loaded.');}
  private loadCostCentersPage():void{this.fetch(true,this.accountingService.getCostCenterPage({...this.costCenterFilters,page:this.costCenterPage,size:10}),p=>{this.costCenters=p.content;this.costCenterTotalPages=p.totalPages||1;},'Cost centers could not be loaded.');}
  private loadBudgets():void{this.fetch(true,this.accountingService.getBudgetPage({...this.budgetFilters,page:this.budgetPage,size:10}),p=>{this.budgets=p.content;this.budgetTotalPages=p.totalPages||1;},'Budgets could not be loaded.');}
  private loadBudget(id:number):void{this.fetch(true,this.accountingService.getBudget(id),v=>this.budgetForm=v,'Budget could not be loaded.');}
  private loadBudgetActual():void{this.fetch(true,this.accountingService.getBudgetActual(this.budgetActualFilters),v=>this.budgetActual=v,'Budget actual report could not be loaded.');}
  private loadFinancialDashboard():void{this.fetch(true,this.accountingService.getFinancialDashboard(this.dashboardYear),v=>this.financialDashboard=v,'Financial dashboard could not be loaded.');}
  private loadPeriods():void{this.fetch(true,this.accountingService.getPeriods(),v=>this.periods=v,'Accounting periods could not be loaded.');}
  private loadYearEnds():void{this.fetch(true,this.accountingService.getYearEnds(),v=>this.yearEnds=v,'Year-end closings could not be loaded.');}

  private emptyExpense(): Expense {
    return { expenseDate: new Date().toISOString().slice(0, 10), categoryId: null, amount: 0, paymentMethod: 'CASH', referenceNo: '', notes: '', status: 'DRAFT' };
  }

  private emptyAccount(): Account {
    return { accountCode: '', accountName: '', accountType: 'ASSET', parentAccountId: null, status: 'ACTIVE' };
  }

  private emptyJournal(): JournalEntry {
    return { journalDate: new Date().toISOString().slice(0, 10), referenceNo: '', description: '', lines: [this.emptyLine(), this.emptyLine()] };
  }

  private emptyLine(): JournalLine {
    return { accountId: null, costCenterId: null, debit: 0, credit: 0, description: '' };
  }

  private emptyBudget():Budget{const y=new Date().getFullYear();return {fiscalYear:y,periodType:'YEARLY',fromDate:`${y}-01-01`,toDate:`${y}-12-31`,accountId:null,costCenterId:null,amount:0,status:'DRAFT'};}

  private toNumber(value: number | undefined | null): number {
    return Number(value || 0);
  }
}
