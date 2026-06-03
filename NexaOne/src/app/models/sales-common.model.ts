export type SalesOrderStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type PaymentStatus = 'PAID' | 'PARTIAL' | 'DUE';

export interface SalesCustomer {
  id: number;
  name: string;
  phone?: string;
  email?: string;
  address?: string;
}

export interface SalesOrderLineItem {
  productId: number | null;
  productName?: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface SalesInvoiceLineItem {
  productId: number | null;
  productName?: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  tax: number;
  subtotal: number;
}

export interface SalesReturnLineItem {
  productId: number | null;
  productName?: string;
  quantity: number;
  unitPrice: number;
  total: number;
}
