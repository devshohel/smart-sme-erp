import { Status } from './product.model';
import { CustomerReceipt } from './customer-receipt.model';

export interface Customer {
  id?: number;
  customerCode?: string;
  name: string;
  companyName?: string | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  postalCode?: string | null;
  creditLimit?: number | null;
  openingBalance?: number | null;
  currentBalance?: number | null;
  dueBalance?: number | null;
  taxNumber?: string | null;
  status?: Status;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustomerPage {
  content: Customer[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface CustomerSearchParams {
  keyword?: string;
  status?: Status | '';
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface CustomerOption {
  id: number;
  customerCode?: string;
  name: string;
  phone?: string | null;
  currentBalance?: number | null;
}

export interface CustomerTransaction {
  id: number;
  documentNo: string;
  date: string;
  amount: number;
  paid: number;
  due: number;
  status: string;
}

export interface CustomerDetail {
  customer: Customer;
  availableCredit: number;
  balanceStatus: 'Normal' | 'Near Credit Limit' | 'Over Limit' | string;
  totalSalesInvoices: number;
  totalDue: number;
  lastInvoiceDate?: string | null;
  lastPaymentDate?: string | null;
  recentReceipts: CustomerReceipt[];
  recentSalesInvoices: CustomerTransaction[];
  recentSalesReturns: CustomerTransaction[];
}

export interface CustomerLedgerEntry {
  date: string;
  referenceType: string;
  referenceNo: string;
  description: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface CustomerLedger {
  customer: Customer;
  fromDate?: string | null;
  toDate?: string | null;
  openingBalance: number;
  closingBalance: number;
  entries: CustomerLedgerEntry[];
}

export interface CustomerAgingRow {
  customerId: number;
  customerCode: string;
  customerName: string;
  current: number;
  days1To30: number;
  days31To60: number;
  days61To90: number;
  days90Plus: number;
  totalDue: number;
}

export interface CustomerAgingReport {
  fromDate?: string | null;
  toDate?: string | null;
  totalDue: number;
  rows: CustomerAgingRow[];
}
