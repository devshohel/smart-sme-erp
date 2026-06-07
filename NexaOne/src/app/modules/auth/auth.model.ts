import { Status } from '../../models/product.model';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface LoginResponse {
  accessToken: string;
  username: string;
  role: string;
  permissions: string[];
  loginTimestamp: string;
}

export interface Role {
  id: number;
  roleName: string;
  description?: string | null;
}

export interface Permission {
  id: number;
  name: string;
  module: string;
  action: string;
  description?: string | null;
}

export interface AuditFilter {
  fromDate: string;
  toDate: string;
  username: string;
  action: string;
  module: string;
}

export interface ActivityLog {
  id: number;
  userId?: number | null;
  username?: string | null;
  action: string;
  module: string;
  tableName?: string | null;
  recordId?: number | null;
  ipAddress?: string | null;
  details?: string | null;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  userId?: number | null;
  username?: string | null;
  tableName: string;
  recordId?: number | null;
  oldData?: string | null;
  newData?: string | null;
  action: string;
  createdAt: string;
}

export interface LoginHistory {
  id: number;
  userId?: number | null;
  username: string;
  status: string;
  ipAddress?: string | null;
  userAgent?: string | null;
  failureReason?: string | null;
  createdAt: string;
}

export interface CompanySettings {
  id?: number;
  companyName: string;
  businessType?: string | null;
  email?: string | null;
  phone?: string | null;
  address?: string | null;
  city?: string | null;
  country?: string | null;
  logoUrl?: string | null;
  taxNumber?: string | null;
  currency: string;
  timezone: string;
  status: Status;
  createdAt?: string;
  updatedAt?: string;
}

export interface InvoiceSettings {
  id?: number;
  salesInvoicePrefix: string;
  purchaseInvoicePrefix: string;
  salesOrderPrefix: string;
  purchaseOrderPrefix: string;
  nextInvoiceNumber: number;
  invoiceFooterText?: string | null;
  defaultPaymentTerms?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface TaxSettings {
  id?: number;
  taxName: string;
  taxRate: number;
  status: Status;
  defaultTaxEnabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SystemSettings {
  id?: number;
  defaultCurrency: string;
  dateFormat: string;
  numberFormat: string;
  lowStockAlertEnabled: boolean;
  dashboardRefreshEnabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface User {
  id?: number;
  name: string;
  username: string;
  email: string;
  phone?: string | null;
  password?: string | null;
  roleId: number | null;
  roleName?: string;
  status?: Status;
  lastLogin?: string | null;
  createdAt?: string;
  updatedAt?: string;
}
