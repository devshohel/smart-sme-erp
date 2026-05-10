export enum Status {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DRAFT = 'DRAFT'
}

export interface ProductCategory {
  id?: number;
  code: string;
  categoryName: string;
  description?: string;
  status: Status;

  parentCategory?: {
  id: number;
  categoryName?: string;
} | null;

  createdAt?: string;
  updatedAt?: string;
}