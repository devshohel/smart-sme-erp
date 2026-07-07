export type SalesOrderStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CONVERTED' | 'CANCELLED' | 'PENDING';
export type SalesInvoiceStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'POSTED' | 'CLOSED' | 'PARTIAL_PAID' | 'PAID' | 'CANCELLED' | 'REVERSED' | 'PENDING' | 'CONFIRMED' | 'COMPLETED';
export type SalesReturnStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'POSTED' | 'CANCELLED' | 'REVERSED';
export type SalesReturnCondition = 'RESELLABLE' | 'DAMAGED' | 'EXPIRED';
export type SalesReturnRefundMethod = 'ADJUST_DUE' | 'CASH' | 'MOBILE_BANKING' | 'BANK' | 'CREDIT_NOTE' | 'EXCHANGE';
export type PaymentStatus = 'PAID' | 'PARTIAL' | 'DUE';

export interface SalesCustomer {
  id: number;
  customerCode?: string;
  name: string;
  phone?: string;
  email?: string;
  address?: string;
}

export interface SalesOrderLineItem {
  id?: number;
  productId: number | null;
  productName?: string;
  uomId?: number | null;
  uomName?: string;
  quantity: number;
  unitPrice: number;
  discount?: number;
  tax?: number;
  subtotal: number;
}

export interface SalesInvoiceLineItem {
  id?: number;
  productId: number | null;
  productName?: string;
  uomId?: number | null;
  uomName?: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  tax: number;
  subtotal: number;
}

export interface SalesReturnLineItem {
    id?: number;
    invoiceItemId?: number | null;
    productId: number | null;
    productName?: string;
    soldQuantity?: number;
    alreadyReturnedQuantity?: number;
    remainingQuantity?: number;
    quantity: number;
    unitPrice: number;
    discount?: number;
    tax?: number;
    returnReason?: string;
    condition?: SalesReturnCondition;
    restock?: boolean;
    total: number;
}
