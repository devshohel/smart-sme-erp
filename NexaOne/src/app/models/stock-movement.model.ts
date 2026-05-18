export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT' | 'TRANSFER';

export interface StockMovement {
  id?: number;
  movementCode?: string;
  productId?: number;
  productCode?: string;
  productName?: string;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  movementType?: MovementType | string;
  quantity?: number;
  unitCost?: number | null;
  referenceType?: string;
  referenceNo?: string;
  batchNo?: string;
  expiryDate?: string;
  note?: string;
  createdAt?: string;
}
