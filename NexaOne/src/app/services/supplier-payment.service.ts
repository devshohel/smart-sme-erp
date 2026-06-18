import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { PurchaseOrder } from '../models/purchase.model';
import {
  SupplierPayment,
  SupplierPaymentMethod,
  SupplierPaymentPage,
  SupplierPaymentSearchParams,
  SupplierPaymentStatus,
  UnpaidPurchaseOrder
} from '../models/supplier-payment.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class SupplierPaymentService {
  private readonly baseUrl = `${environment.apiUrl}/supplier-payments`;

  constructor(private http: HttpClient) {}

  getPaymentPage(params: SupplierPaymentSearchParams): Observable<SupplierPaymentPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10))
      .set('sort', params.sort || 'paymentDate')
      .set('direction', params.direction || 'desc');

    if (params.keyword?.trim()) {
      httpParams = httpParams.set('keyword', params.keyword.trim());
    }
    if (params.supplierId) {
      httpParams = httpParams.set('supplierId', String(params.supplierId));
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status as SupplierPaymentStatus);
    }
    if (params.paymentMethod) {
      httpParams = httpParams.set('paymentMethod', params.paymentMethod as SupplierPaymentMethod);
    }
    if (params.fromDate) {
      httpParams = httpParams.set('fromDate', params.fromDate);
    }
    if (params.toDate) {
      httpParams = httpParams.set('toDate', params.toDate);
    }

    return this.http
      .get<SupplierPaymentPage | ApiResponse<SupplierPaymentPage>>(`${this.baseUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getPaymentById(id: number): Observable<SupplierPayment> {
    return this.http
      .get<SupplierPayment | ApiResponse<SupplierPayment>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getUnpaidPurchases(supplierId: number): Observable<UnpaidPurchaseOrder[]> {
    const params = new HttpParams().set('supplierId', String(supplierId));
    return this.http
      .get<PurchaseOrder[] | ApiResponse<PurchaseOrder[]>>(`${environment.apiUrl}/purchases/orders/unpaid`, { params })
      .pipe(map(response => unwrapApiResponse(response) as UnpaidPurchaseOrder[]));
  }

  createPayment(payment: SupplierPayment): Observable<SupplierPayment> {
    return this.http
      .post<SupplierPayment | ApiResponse<SupplierPayment>>(this.baseUrl, payment)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updatePayment(id: number, payment: SupplierPayment): Observable<SupplierPayment> {
    return this.http
      .put<SupplierPayment | ApiResponse<SupplierPayment>>(`${this.baseUrl}/${id}`, payment)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  postPayment(id: number): Observable<SupplierPayment> {
    return this.http
      .post<SupplierPayment | ApiResponse<SupplierPayment>>(`${this.baseUrl}/${id}/post`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  cancelPayment(id: number): Observable<SupplierPayment> {
    return this.http
      .post<SupplierPayment | ApiResponse<SupplierPayment>>(`${this.baseUrl}/${id}/cancel`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  reversePayment(id: number, reversalReason: string): Observable<SupplierPayment> {
    return this.http
      .post<SupplierPayment | ApiResponse<SupplierPayment>>(`${this.baseUrl}/${id}/reverse`, { reversalReason })
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
