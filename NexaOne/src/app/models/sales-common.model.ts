export type SalesOrderStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CONVERTED' | 'CANCELLED' | 'PENDING';
export type SalesInvoiceStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'POSTED' | 'PARTIAL_PAID' | 'PAID' | 'CANCELLED' | 'REVERSED' | 'PENDING' | 'CONFIRMED' | 'COMPLETED';
export type SalesReturnStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'POSTED' | 'CANCELLED';
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
  productId: number | null;
  productName?: string;
  quantity: number;
  unitPrice: number;
  total: number;
}
