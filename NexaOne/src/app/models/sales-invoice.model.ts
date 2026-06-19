import { PaymentStatus, SalesCustomer, SalesInvoiceLineItem, SalesInvoiceStatus } from './sales-common.model';

export interface SalesInvoice {
  id?: number;
  invoiceNo?: string;
  orderId?: number | null;
  orderNo?: string;
  customerId: number | null;
  customerName?: string;
  customer?: SalesCustomer;
  warehouseId: number | null;
  warehouseName?: string;
  saleDate: string;
  notes?: string;
  items: SalesInvoiceLineItem[];
  totalAmount: number;
  discountAmount: number;
  taxAmount: number;
  netTotal: number;
  paidAmount: number;
  dueAmount: number;
  paymentStatus: PaymentStatus;
  status?: SalesInvoiceStatus;
  submittedAt?: string;
  submittedBy?: string;
  approvedAt?: string;
  approvedBy?: string;
  postedAt?: string;
  postedBy?: string;
  cancelledAt?: string;
  cancelledBy?: string;
  reversedAt?: string;
  reversedBy?: string;
  reversalReason?: string;
}
