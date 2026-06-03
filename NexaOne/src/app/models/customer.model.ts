import { Status } from './product.model';

export interface Customer {
  id?: number;
  customerCode?: string;
  name: string;
  companyName?: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
  creditLimit?: number | null;
  openingBalance?: number | null;
  currentBalance?: number | null;
  taxNumber?: string;
  status?: Status;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
}
