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
  imageOriginalFilename?: string;
  imageStoredFilename?: string;
  imageContentType?: string;
  imageSize?: number | null;
  imagePath?: string;
  status?: Status;
  categoryId?: number | null;
  categoryName?: string;
  brandId?: number | null;
  brandName?: string;
  uomId?: number | null;
  uomName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductFormPayload {
  product: Product;
  imageFile?: File | null;
}

export interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ProductStats {
  totalProducts: number;
  activeProducts: number;
  inactiveProducts: number;
  productsWithoutImage: number;
}

export interface ProductSearchParams {
  keyword?: string;
  categoryId?: number | null;
  brandId?: number | null;
  status?: Status | '';
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
}
