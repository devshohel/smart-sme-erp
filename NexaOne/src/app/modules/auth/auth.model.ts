import { Status } from '../../models/product.model';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  username: string;
  role: string;
  loginTimestamp: string;
}

export interface Role {
  id: number;
  roleName: string;
  description?: string | null;
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
