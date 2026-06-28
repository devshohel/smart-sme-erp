import { SalesCustomer, SalesInvoiceStatus, SalesReturnLineItem, SalesReturnRefundMethod, SalesReturnStatus } from './sales-common.model';

export interface SalesReturn {
  id?: number;
  returnNo?: string;
  returnCode?: string;
  invoiceId: number | null;
  invoiceNo?: string;
  customerId: number | null;
  customerName?: string;
  warehouseId?: number | null;
  warehouseName?: string;
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
  cancellationReason?: string;
  refundMethod?: SalesReturnRefundMethod;
  items: SalesReturnLineItem[];
  totalAmount: number;
}

export interface SalesReturnContextItem {
  invoiceItemId: number;
  productId: number;
  productName: string;
  soldQuantity: number;
  returnedQuantity: number;
  remainingQuantity: number;
  unitPrice: number;
  discount: number;
  tax: number;
}

export interface SalesReturnContext {
  invoiceId: number;
  invoiceNo: string;
  customerId: number;
  customerName: string;
  warehouseId: number;
  warehouseName: string;
  saleDate: string;
  status: SalesInvoiceStatus;
  paidAmount: number;
  dueAmount: number;
  paidRefundSupported: boolean;
  limitationMessage?: string;
  items: SalesReturnContextItem[];
}
