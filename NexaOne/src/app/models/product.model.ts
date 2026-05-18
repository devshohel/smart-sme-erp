export type ProductType = 'STORABLE' | 'SERVICE' | 'CONSUMABLE';
export type Status = 'ACTIVE' | 'INACTIVE' | 'DRAFT';

export interface Product {
  id?: number;
  productCode?: string;
  productName: string;
  sku: string;
  barcode?: string;
  type?: ProductType;
  purchasePrice: number;
  salePrice: number;
  taxPercentage?: number | null;
  reorderLevel?: number | null;
  imageUrl?: string;
  status?: Status;
  categoryId?: number | null;
  categoryName?: string;
  brandId?: number | null;
  brandName?: string;
  uomId?: number | null;
  uomName?: string;
}
