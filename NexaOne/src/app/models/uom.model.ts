import { Status } from './product.model';

export interface Uom {
  id?: number;
  code: string;
  name: string;
  type?: string;
  conversionFactor?: number | null;
  status: Status;
}
