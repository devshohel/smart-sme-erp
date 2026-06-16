export interface Stock {
  id?: number;
  productId: number;
  productName?: string;
  sku?: string;
  barcode?: string;
  categoryId?: number;
  categoryName?: string;
  warehouseId: number;
  warehouseName?: string;
  quantity: number;
  reorderLevel?: number | null;
}

export interface StockPage {
  content: Stock[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface StockSearchParams {
  keyword?: string;
  warehouseId?: number | null;
  categoryId?: number | null;
  lowStockOnly?: boolean;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}
