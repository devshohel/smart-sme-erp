import { Status } from './product.model';

export interface Customer {
  id?: number;
  customerCode?: string;
  name: string;
  companyName?: string | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  postalCode?: string | null;
  creditLimit?: number | null;
  openingBalance?: number | null;
  currentBalance?: number | null;
  taxNumber?: string | null;
  status?: Status;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
}
