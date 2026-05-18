export interface Stock {
  id?: number;
  productId: number;
  productName?: string;
  warehouseId: number;
  warehouseName?: string;
  quantity: number;
  reorderLevel?: number | null;
}
