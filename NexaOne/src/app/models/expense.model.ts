export type ExpenseStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'POSTED' | 'REVERSED' | 'CANCELLED';
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
  submittedAt?: string;
  submittedBy?: string;
  approvedAt?: string;
  approvedBy?: string;
  rejectedAt?: string;
  rejectedBy?: string;
  rejectionReason?: string | null;
  approvalComment?: string | null;
  receiptOriginalFilename?: string | null;
  receiptStoredFilename?: string | null;
  receiptContentType?: string | null;
  receiptSize?: number | null;
  receiptUrl?: string | null;
  journalEntryId?: number | null;
  taxApplicable?: boolean;
  taxRate?: number | null;
  taxAmount?: number | null;
  netAmount?: number | null;
  grossAmount?: number | null;
  reversedAt?: string;
  reversedBy?: string;
  reversalReason?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExpenseReportRow {
  label: string;
  netAmount: number;
  taxAmount: number;
  grossAmount: number;
  count: number;
}

export interface ExpensePage {
  content: Expense[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
