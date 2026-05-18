import { Status } from './product.model';

export interface Brand {
  id?: number;
  code: string;
  brandName: string;
  status: Status;
}
