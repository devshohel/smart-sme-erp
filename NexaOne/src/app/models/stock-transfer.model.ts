export type StockTransferStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'IN_TRANSIT' | 'RECEIVED' | 'CANCELLED';

export interface StockTransferItem {
  id?: number;
  productId: number | null;
  productName?: string;
  sku?: string;
  quantity: number | null;
  remarks?: string;
  currentStock?: number | null;
}

export interface StockTransfer {
  id?: number;
  transferNo?: string;
  fromWarehouseId: number | null;
  fromWarehouseName?: string;
  toWarehouseId: number | null;
  toWarehouseName?: string;
  status: StockTransferStatus;
  transferDate: string;
  expectedDate?: string | null;
  remarks?: string;
  createdAt?: string;
  updatedAt?: string;
  approvedAt?: string;
  receivedAt?: string;
  items: StockTransferItem[];
}

export interface StockTransferPage {
  content: StockTransfer[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface StockTransferSearchParams {
  keyword?: string;
  fromWarehouseId?: number | null;
  toWarehouseId?: number | null;
  status?: StockTransferStatus | '';
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}
