import { SalesCustomer, SalesReturnLineItem, SalesReturnStatus } from './sales-common.model';

export interface SalesReturn {
  id?: number;
  returnNo?: string;
  returnCode?: string;
  invoiceId: number | null;
  invoiceNo?: string;
  customerId: number | null;
  customerName?: string;
  customer?: SalesCustomer;
  returnDate: string;
  notes?: string;
  status?: SalesReturnStatus;
  submittedAt?: string;
  submittedBy?: string;
  approvedAt?: string;
  approvedBy?: string;
  rejectedAt?: string;
  rejectedBy?: string;
  rejectionReason?: string;
  postedAt?: string;
  postedBy?: string;
  cancelledAt?: string;
  cancelledBy?: string;
  items: SalesReturnLineItem[];
  totalAmount: number;
}
