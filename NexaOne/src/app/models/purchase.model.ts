export type PurchaseStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'APPROVED'
  | 'RECEIVED'
  | 'PARTIAL_PAID'
  | 'PAID'
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
  items: PurchaseItem[];
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
  createdBy?: number | null;
  createdAt?: string;
  items: PurchaseReturnItem[];
}
