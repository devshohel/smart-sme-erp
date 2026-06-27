import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SalesInvoiceLineItem, SalesInvoiceStatus } from '../models/sales-common.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

export type PosPaymentMethod = 'CASH' | 'CARD' | 'MOBILE_BANKING' | 'BANK' | 'DUE' | 'OTHER';

export interface PosSaleItemRequest {
  productId: number;
  quantity: number;
  unitPrice?: number;
  discount?: number;
  tax?: number;
}

export interface PosPaymentRequest {
  paymentMethod: PosPaymentMethod;
  paidAmount: number;
  referenceNo?: string;
}

export interface PosSaleRequest {
  customerId: number;
  warehouseId: number;
  saleDate: string;
  items: PosSaleItemRequest[];
  payment: PosPaymentRequest;
  notes?: string;
}

export interface PosSaleResponse {
  invoiceId: number;
  invoiceNo: string;
  customerId: number;
  customerName: string;
  warehouseId: number;
  warehouseName: string;
  items: SalesInvoiceLineItem[];
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  grandTotal: number;
  paidAmount: number;
  dueAmount: number;
  paymentMethod: PosPaymentMethod;
  status: SalesInvoiceStatus;
  saleDate: string;
  receiptId?: number;
  receiptNo?: string;
}

@Injectable({ providedIn: 'root' })
export class SalesPosService {
  private readonly baseUrl = `${environment.apiUrl}/sales/pos`;
  readonly completionAvailable = true;

  constructor(private http: HttpClient) {}

  completeSale(request: PosSaleRequest): Observable<PosSaleResponse> {
    return this.http
      .post<PosSaleResponse | ApiResponse<PosSaleResponse>>(`${this.baseUrl}/complete`, request)
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
