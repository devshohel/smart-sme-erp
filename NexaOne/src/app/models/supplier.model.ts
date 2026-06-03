import { Status } from './product.model';

export interface Supplier {
  id?: number;
  supplierCode?: string;
  name: string;
  companyName?: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
  openingBalance?: number | null;
  currentBalance?: number | null;
  taxNumber?: string;
  bankAccount?: string;
  paymentTerms?: string;
  status?: Status;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
}
