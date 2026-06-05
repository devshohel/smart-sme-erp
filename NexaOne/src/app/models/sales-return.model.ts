import { SalesCustomer, SalesReturnLineItem } from './sales-common.model';

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
  items: SalesReturnLineItem[];
  totalAmount: number;
}
