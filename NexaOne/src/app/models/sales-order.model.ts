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
  submittedAt?: string;
  submittedBy?: string;
  approvedAt?: string;
  approvedBy?: string;
  rejectedAt?: string;
  rejectedBy?: string;
  rejectionReason?: string;
  cancelledAt?: string;
  cancelledBy?: string;
  convertedAt?: string;
  convertedBy?: string;
  items: SalesOrderLineItem[];
  grandTotal: number;
}
