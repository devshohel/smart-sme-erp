export type ExpenseStatus = 'DRAFT' | 'POSTED' | 'CANCELLED';
export type ExpensePaymentMethod = 'CASH' | 'BANK' | 'MOBILE_BANKING' | 'OTHER';

export interface ExpenseCategoryOption {
  id: number;
  name: string;
  accountId?: number | null;
  accountCode?: string | null;
  accountName?: string | null;
  status?: string;
}

export interface Expense {
  id?: number;
  expenseNo?: string;
  expenseDate: string;
  categoryId: number | null;
  categoryName?: string;
  amount: number;
  paymentMethod: ExpensePaymentMethod;
  referenceNo?: string | null;
  notes?: string | null;
  status?: ExpenseStatus;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExpensePage {
  content: Expense[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
