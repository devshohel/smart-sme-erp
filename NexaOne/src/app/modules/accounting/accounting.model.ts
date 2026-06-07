export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'INCOME' | 'EXPENSE';
export type AccountingStatus = 'ACTIVE' | 'INACTIVE';
export type PaymentMethod = 'CASH' | 'BANK' | 'OTHER';
export type ExpenseStatus = 'ACTIVE' | 'CANCELLED';
export type JournalStatus = 'DRAFT' | 'POSTED' | 'CANCELLED';

export interface ExpenseCategory {
  id?: number;
  name: string;
  description?: string | null;
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
  description?: string | null;
  status?: JournalStatus;
  lines: JournalLine[];
  createdAt?: string;
  updatedAt?: string;
}

export interface BookEntry {
  date: string;
  reference: string;
  description: string;
  moneyIn: number;
  moneyOut: number;
  balance: number;
}
