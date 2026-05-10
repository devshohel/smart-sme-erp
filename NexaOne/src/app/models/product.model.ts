export type ProductType = 'STORABLE' | 'SERVICE' | 'CONSUMABLE';
export type Status = 'ACTIVE' | 'INACTIVE' | 'DRAFT';

export interface Product {
  id?: number;

  productCode: string;
  productName: string;
  sku: string;
  barcode?: string;

  type: ProductType;

  category?: { id: number };
  brand?: { id: number };

  purchasePrice?: number;
  salePrice?: number;
  taxPercentage?: number;

  reorderLevel?: number;

  status: Status;
}