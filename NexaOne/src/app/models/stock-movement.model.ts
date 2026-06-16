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
  quantityBefore?: number;
  quantityChange?: number;
  quantityAfter?: number;
  unitCost?: number | null;
  referenceType?: string;
  referenceNo?: string;
  batchNo?: string;
  expiryDate?: string;
  note?: string;
  createdAt?: string;
}

export interface StockMovementPage {
  content: StockMovement[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface StockMovementSearchParams {
  keyword?: string;
  productId?: number | null;
  warehouseId?: number | null;
  movementType?: MovementType | string;
  referenceType?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface StockCard {
  productId: number;
  productName?: string;
  warehouseId: number;
  warehouseName?: string;
  currentQuantity: number;
  movements: StockMovement[];
}
