export type PurchaseStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'PARTIAL_RECEIVED'
  | 'RECEIVED'
  | 'POSTED'
  | 'PARTIAL_PAID'
  | 'PAID'
  | 'REVERSED'
  | 'CANCELLED';

export interface PurchaseItem {
  id?: number;
  productId: number | null;
  productName?: string;
  uomId?: number | null;
  uomName?: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  tax: number;
  subTotal: number;
  receivedQuantity?: number;
  returnedQuantity?: number;
}

export interface PurchaseOrder {
  id?: number;
  purchaseCode?: string;
  supplierId: number | null;
  supplierName?: string;
  warehouseId: number | null;
  warehouseName?: string;
  purchaseDate: string;
  totalAmount: number;
  discountAmount: number;
  taxAmount: number;
  netTotal: number;
  paidAmount: number;
  dueAmount: number;
  status: PurchaseStatus;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
  submittedBy?: string | null;
  submittedAt?: string;
  approvedBy?: string | null;
  approvedAt?: string;
  rejectedBy?: string | null;
  rejectedAt?: string;
  rejectionReason?: string | null;
  cancelledBy?: string | null;
  cancelledAt?: string;
  items: PurchaseItem[];
}

export type PurchaseReceiveStatus = 'POSTED' | 'CANCELLED';

export interface PurchaseReceiveItem {
  id?: number;
  productId: number | null;
  productName?: string;
  orderedQty?: number;
  receivedQty: number;
  remainingQty?: number;
}

export interface PurchaseReceive {
  id?: number;
  grnNo?: string;
  purchaseOrderId: number | null;
  purchaseCode?: string;
  warehouseId?: number | null;
  warehouseName?: string;
  receiveDate: string;
  status?: PurchaseReceiveStatus;
  notes?: string;
  createdAt?: string;
  postedAt?: string;
  items: PurchaseReceiveItem[];
}

export interface PurchaseReturnItem {
  id?: number;
  productId: number | null;
  productName?: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface PurchaseReturn {
  id?: number;
  returnCode?: string;
  purchaseId: number | null;
  purchaseCode?: string;
  supplierId: number | null;
  supplierName?: string;
  returnDate: string;
  totalAmount: number;
  status?: PurchaseStatus;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
  submittedBy?: string | null;
  submittedAt?: string;
  approvedBy?: string | null;
  approvedAt?: string;
  rejectedBy?: string | null;
  rejectedAt?: string;
  rejectionReason?: string | null;
  postedBy?: string | null;
  postedAt?: string;
  cancelledBy?: string | null;
  cancelledAt?: string;
  notes?: string;
  items: PurchaseReturnItem[];
}
