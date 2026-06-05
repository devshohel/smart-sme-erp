import { Status } from './product.model';

export interface Supplier {
  id?: number;
  supplierCode?: string;
  name: string;
  companyName?: string | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  postalCode?: string | null;
  openingBalance?: number | null;
  currentBalance?: number | null;
  taxNumber?: string | null;
  bankAccount?: string | null;
  paymentTerms?: string | null;
  status?: Status;
  createdBy?: number | null;
  createdAt?: string;
  updatedAt?: string;
}
