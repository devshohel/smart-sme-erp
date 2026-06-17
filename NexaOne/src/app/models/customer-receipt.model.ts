import { SalesInvoice } from './sales-invoice.model';

export type CustomerReceiptStatus = 'DRAFT' | 'POSTED' | 'CANCELLED';
export type CustomerReceiptPaymentMethod = 'CASH' | 'BANK' | 'MOBILE_BANKING' | 'CHEQUE' | 'OTHER';
export type CustomerReceiptAllocationMode = 'AUTO' | 'MANUAL';

export interface CustomerReceiptAllocation {
  id?: number;
  salesInvoiceId: number;
  invoiceNo?: string;
  invoiceDate?: string;
  netTotal?: number;
  paidAmount?: number;
  dueAmount?: number;
  allocatedAmount: number;
  createdAt?: string;
}

export interface CustomerReceipt {
  id?: number;
  receiptNo?: string;
  customerId: number;
  customerCode?: string;
  customerName?: string;
  receiptDate: string;
  paymentMethod: CustomerReceiptPaymentMethod;
  amount: number;
  referenceNo?: string | null;
  notes?: string | null;
  allocationMode?: CustomerReceiptAllocationMode;
  totalAllocatedAmount?: number | null;
  unappliedAmount?: number | null;
  allocations?: CustomerReceiptAllocation[];
  status?: CustomerReceiptStatus;
  postedAt?: string | null;
  cancelledAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
  journalNo?: string | null;
}

export interface CustomerReceiptPage {
  content: CustomerReceipt[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface CustomerReceiptSearchParams {
  keyword?: string;
  customerId?: number | '';
  status?: CustomerReceiptStatus | '';
  paymentMethod?: CustomerReceiptPaymentMethod | '';
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface UnpaidSalesInvoice extends SalesInvoice {}
