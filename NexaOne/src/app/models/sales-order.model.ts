import { SalesCustomer, SalesOrderLineItem, SalesOrderStatus } from './sales-common.model';

export interface SalesOrder {
  id?: number;
  orderNo?: string;
  customerId: number | null;
  customerName?: string;
  customer?: SalesCustomer;
  warehouseId: number | null;
  warehouseName?: string;
  orderDate: string;
  notes?: string;
  status: SalesOrderStatus;
  items: SalesOrderLineItem[];
  grandTotal: number;
}
