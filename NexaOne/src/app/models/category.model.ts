import { Status } from './product.model';

export interface ProductCategory {
  id?: number;
  code: string;
  categoryName: string;
  description?: string;
  status: Status;
  parentCategoryId?: number | null;
  parentCategoryName?: string;
  createdAt?: string;
  updatedAt?: string;
}
