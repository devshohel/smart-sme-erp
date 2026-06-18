import { PurchaseOrder } from './purchase.model';

export type SupplierPaymentStatus = 'DRAFT' | 'POSTED' | 'CANCELLED' | 'REVERSED';
export type SupplierPaymentMethod = 'CASH' | 'BANK' | 'MOBILE_BANKING' | 'CHEQUE' | 'OTHER';
export type SupplierPaymentAllocationMode = 'AUTO' | 'MANUAL';

export interface SupplierPaymentAllocation {
  id?: number;
  purchaseOrderId: number;
  purchaseCode?: string;
  purchaseDueAmount?: number;
  allocatedAmount: number;
}

export interface SupplierPayment {
  id?: number;
  paymentNo?: string;
  supplierId: number;
  supplierCode?: string;
  supplierName?: string;
  paymentDate: string;
  paymentMethod: SupplierPaymentMethod;
  amount: number;
  referenceNo?: string | null;
  notes?: string | null;
  allocationMode?: SupplierPaymentAllocationMode;
  totalAllocatedAmount?: number | null;
  unappliedAmount?: number | null;
  allocations?: SupplierPaymentAllocation[];
  status?: SupplierPaymentStatus;
  postedAt?: string | null;
  cancelledAt?: string | null;
  reversedAt?: string | null;
  reversalReason?: string | null;
  reversedBy?: number | null;
  canReverse?: boolean;
  createdAt?: string;
  updatedAt?: string;
  journalNo?: string | null;
}

export interface SupplierPaymentPage {
  content: SupplierPayment[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface SupplierPaymentSearchParams {
  keyword?: string;
  supplierId?: number | '';
  status?: SupplierPaymentStatus | '';
  paymentMethod?: SupplierPaymentMethod | '';
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface UnpaidPurchaseOrder extends PurchaseOrder {}
