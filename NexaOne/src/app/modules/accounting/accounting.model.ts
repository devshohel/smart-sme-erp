export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'INCOME' | 'EXPENSE';
export type AccountingStatus = 'ACTIVE' | 'INACTIVE';
export type PaymentMethod = 'CASH' | 'BANK' | 'MOBILE_BANKING' | 'OTHER';
export type ExpenseStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'POSTED' | 'REVERSED' | 'CANCELLED';
export type JournalStatus = 'DRAFT' | 'POSTED' | 'CANCELLED';

export interface ExpenseCategory {
  id?: number;
  name: string;
  description?: string | null;
  accountId?: number | null;
  accountCode?: string | null;
  accountName?: string | null;
  status?: AccountingStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface Expense {
  id?: number;
  expenseNo?: string;
  expenseDate: string;
  categoryId: number | null;
  categoryName?: string;
  amount: number;
  paymentMethod: PaymentMethod;
  referenceNo?: string | null;
  notes?: string | null;
  status?: ExpenseStatus;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Account {
  id?: number;
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  parentAccountId?: number | null;
  parentAccountName?: string | null;
  status?: AccountingStatus;
}

export interface JournalLine {
  id?: number;
  accountId: number | null;
  accountCode?: string;
  accountName?: string;
  debit: number;
  credit: number;
  description?: string | null;
}

export interface JournalEntry {
  id?: number;
  journalNo?: string;
  journalDate: string;
  referenceNo?: string | null;
  referenceType?: string | null;
  description?: string | null;
  status?: JournalStatus;
  lines: JournalLine[];
  createdAt?: string;
  updatedAt?: string;
  postedAt?: string;
  cancelledAt?: string;
  createdBy?: string;
  postedBy?: string;
  cancelledBy?: string;
  totalDebit?: number;
  totalCredit?: number;
  sourceType?: string | null;
}

export interface BookEntry {
  date: string;
  journalNo: string;
  referenceType?: string;
  referenceNo?: string;
  description: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface AccountingBook {
  openingBalance: number;
  closingBalance: number;
  rows: BookEntry[];
}

export interface LedgerEntry {
  date: string;
  account: string;
  referenceNo: string;
  description: string;
  debit: number;
  credit: number;
  balance: number;
}

export interface TrialBalanceRow {
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  debitBalance: number;
  creditBalance: number;
}

export interface TrialBalance {
  rows: TrialBalanceRow[];
  totalDebit: number;
  totalCredit: number;
  difference: number;
  balanced: boolean;
}

export interface GeneralLedgerRow {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  openingBalance: number;
  totalDebit: number;
  totalCredit: number;
  closingBalance: number;
}

export interface GeneralLedger {
  accounts: GeneralLedgerRow[];
  totalDebit: number;
  totalCredit: number;
  outOfBalance: boolean;
  differenceAmount: number;
}

export interface AccountLedgerEntry {
  date: string;
  journalNo: string;
  referenceType?: string;
  referenceNo?: string;
  description?: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface AccountLedger {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  openingBalance: number;
  closingBalance: number;
  transactions: AccountLedgerEntry[];
}

export interface FinancialStatementLine {
  accountId: number;
  accountCode: string;
  accountName: string;
  groupName: string;
  amount: number;
}

export interface ProfitLoss {
  income: FinancialStatementLine[];
  expenses: FinancialStatementLine[];
  totalIncome: number;
  totalExpense: number;
  netProfitLoss: number;
  outOfBalance: boolean;
  differenceAmount: number;
}

export interface BalanceSheet {
  assets: FinancialStatementLine[];
  liabilities: FinancialStatementLine[];
  equity: FinancialStatementLine[];
  totalAssets: number;
  totalLiabilities: number;
  ownerCapital: number;
  retainedEarnings: number;
  currentProfitLoss: number;
  totalEquity: number;
  liabilitiesAndEquity: number;
  outOfBalance: boolean;
  differenceAmount: number;
}
