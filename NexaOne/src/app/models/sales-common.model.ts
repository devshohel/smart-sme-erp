export type SalesOrderStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'CANCELLED';
export type SalesInvoiceStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type PaymentStatus = 'PAID' | 'PARTIAL' | 'DUE';

export interface SalesCustomer {
  id: number;
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
