import { Status } from './product.model';
import { SupplierPayment } from './supplier-payment.model';

export interface Supplier {
  id?: number;
  supplierCode?: string;
  name: string;
  companyName?: string | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  postalCode?: string | null;
  openingBalance?: number | null;
  currentBalance?: number | null;
  supplierDue?: number | null;
  taxNumber?: string | null;
  bankAccount?: string | null;
  paymentTerms?: string | null;
  status?: Status;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface SupplierPage {
  content: Supplier[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface SupplierOption {
  id: number;
  supplierCode?: string | null;
  name: string;
  phone?: string | null;
  status?: Status;
}

export interface SupplierPurchaseSummary {
  purchaseNumber: string;
  date: string;
  netTotal: number;
  paidAmount: number;
  dueAmount: number;
  status: string;
}

export interface SupplierReturnSummary {
  returnNumber: string;
  date: string;
  amount: number;
  status: string;
}

export interface SupplierDetail {
  supplier: Supplier;
  supplierDue: number;
  recentPurchases: SupplierPurchaseSummary[];
  recentPurchaseReturns: SupplierReturnSummary[];
  recentPayments: SupplierPayment[];
}

export interface SupplierLedgerEntry {
  date: string;
  referenceType: string;
  referenceNo: string;
  description: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface SupplierLedger {
  supplier: Supplier;
  fromDate?: string | null;
  toDate?: string | null;
  openingBalance: number;
  closingBalance: number;
  entries: SupplierLedgerEntry[];
}

export interface SupplierStatementRow {
  date: string;
  referenceType: string;
  referenceNo: string;
  description: string;
  debit: number;
  credit: number;
  runningBalance: number;
  advanceAmount: number;
  status: string;
}

export interface SupplierStatement {
  supplier: Supplier;
  fromDate?: string | null;
  toDate?: string | null;
  openingBalance: number;
  closingPayableBalance: number;
  supplierAdvanceBalance: number;
  netSupplierPosition: number;
  rows: SupplierStatementRow[];
}

export interface SupplierAgingRow {
  supplierId: number;
  supplierCode: string;
  supplierName: string;
  current: number;
  days1To30: number;
  days31To60: number;
  days61To90: number;
  days90Plus: number;
  totalDue: number;
}

export interface SupplierAgingReport {
  fromDate?: string | null;
  toDate?: string | null;
  totalDue: number;
  rows: SupplierAgingRow[];
}

export type ApReconciliationStatus = 'MATCHED' | 'VARIANCE' | 'REVIEW_NEEDED';

export interface ApReconciliationSummary {
  totalPurchaseDue: number;
  totalSupplierAdvance: number;
  totalGlAccountsPayable: number;
  totalVariance: number;
  netSupplierExposure: number;
}

export interface ApReconciliationRow {
  supplierId?: number | null;
  supplierCode: string;
  supplierName: string;
  purchaseDue: number;
  supplierAdvance: number;
  glAccountsPayable: number;
  variance: number;
  netExposure: number;
  status: ApReconciliationStatus;
}

export interface ApReconciliationBreakdown {
  purchaseGross: number;
  purchaseReturns: number;
  allocatedPayments: number;
  paymentReversals: number;
  supplierAdvance: number;
  manualApAdjustments: number;
}

export interface ApReconciliationReport {
  summary: ApReconciliationSummary;
  rows: ApReconciliationRow[];
  breakdown: ApReconciliationBreakdown;
}
