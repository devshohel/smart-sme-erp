import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SalesReturn, SalesReturnContext } from '../models/sales-return.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class SalesReturnService {
  private readonly baseUrl = `${environment.apiUrl}/sales/returns`;

  constructor(private http: HttpClient) {}

  getAllReturns(): Observable<SalesReturn[]> {
    return this.http
      .get<SalesReturn[] | ApiResponse<SalesReturn[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response).map(item => this.normalizeReturn(item))));
  }

  getReturnContext(invoiceId: number): Observable<SalesReturnContext> {
    return this.http.get<SalesReturnContext | ApiResponse<SalesReturnContext>>(`${this.baseUrl}/context/${invoiceId}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveReturn(salesReturn: SalesReturn): Observable<SalesReturn> {
    const payload = this.normalizeReturn(salesReturn);

    const request$ = payload.id
      ? this.http.put<SalesReturn | ApiResponse<SalesReturn>>(`${this.baseUrl}/${payload.id}`, payload)
      : this.http.post<SalesReturn | ApiResponse<SalesReturn>>(this.baseUrl, payload);

    return request$
      .pipe(map(response => this.normalizeReturn(unwrapApiResponse(response))));
  }

  submitReturn(id: number): Observable<SalesReturn> {
    return this.http.post<SalesReturn | ApiResponse<SalesReturn>>(`${this.baseUrl}/${id}/submit`, {})
      .pipe(map(response => this.normalizeReturn(unwrapApiResponse(response))));
  }

  approveReturn(id: number): Observable<SalesReturn> {
    return this.http.post<SalesReturn | ApiResponse<SalesReturn>>(`${this.baseUrl}/${id}/approve`, {})
      .pipe(map(response => this.normalizeReturn(unwrapApiResponse(response))));
  }

  rejectReturn(id: number, reason: string): Observable<SalesReturn> {
    return this.http.post<SalesReturn | ApiResponse<SalesReturn>>(`${this.baseUrl}/${id}/reject`, { reason })
      .pipe(map(response => this.normalizeReturn(unwrapApiResponse(response))));
  }

  postReturn(id: number): Observable<SalesReturn> {
    return this.http.post<SalesReturn | ApiResponse<SalesReturn>>(`${this.baseUrl}/${id}/post`, {})
      .pipe(map(response => this.normalizeReturn(unwrapApiResponse(response))));
  }

  cancelReturn(id: number, reason: string): Observable<SalesReturn> {
    return this.http.post<SalesReturn | ApiResponse<SalesReturn>>(`${this.baseUrl}/${id}/cancel`, { reason })
      .pipe(map(response => this.normalizeReturn(unwrapApiResponse(response))));
  }

  private normalizeReturn(salesReturn: SalesReturn): SalesReturn {
    return {
      ...salesReturn,
      returnNo: salesReturn.returnNo || salesReturn.returnCode,
      returnCode: salesReturn.returnCode || salesReturn.returnNo,
      invoiceId: salesReturn.invoiceId ?? null,
      customerId: salesReturn.customerId ?? null,
      returnDate: this.toApiDateTime(salesReturn.returnDate),
      status: salesReturn.status || 'DRAFT',
      totalAmount: Number(salesReturn.totalAmount || 0),
      items: (salesReturn.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        invoiceItemId: item.invoiceItemId ?? null,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        discount: Number(item.discount || 0),
        tax: Number(item.tax || 0),
        condition: item.condition || 'RESELLABLE',
        restock: item.restock !== false,
        total: Number(item.total || 0)
      }))
    };
  }

  private toApiDateTime(value: string): string {
    return value && value.length === 10 ? `${value}T00:00:00` : value;
  }
}
