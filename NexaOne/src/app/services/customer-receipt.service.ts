import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  CustomerReceipt,
  CustomerReceiptPage,
  CustomerReceiptPaymentMethod,
  CustomerReceiptSearchParams,
  CustomerReceiptStatus
} from '../models/customer-receipt.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class CustomerReceiptService {
  private readonly baseUrl = `${environment.apiUrl}/customer-receipts`;

  constructor(private http: HttpClient) {}

  getReceiptPage(params: CustomerReceiptSearchParams): Observable<CustomerReceiptPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10))
      .set('sort', params.sort || 'receiptDate')
      .set('direction', params.direction || 'desc');

    if (params.keyword?.trim()) {
      httpParams = httpParams.set('keyword', params.keyword.trim());
    }
    if (params.customerId) {
      httpParams = httpParams.set('customerId', String(params.customerId));
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status as CustomerReceiptStatus);
    }
    if (params.paymentMethod) {
      httpParams = httpParams.set('paymentMethod', params.paymentMethod as CustomerReceiptPaymentMethod);
    }
    if (params.fromDate) {
      httpParams = httpParams.set('fromDate', params.fromDate);
    }
    if (params.toDate) {
      httpParams = httpParams.set('toDate', params.toDate);
    }

    return this.http
      .get<CustomerReceiptPage | ApiResponse<CustomerReceiptPage>>(`${this.baseUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getReceipts(): Observable<CustomerReceipt[]> {
    return this.http
      .get<CustomerReceipt[] | ApiResponse<CustomerReceipt[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getReceiptById(id: number): Observable<CustomerReceipt> {
    return this.http
      .get<CustomerReceipt | ApiResponse<CustomerReceipt>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  createReceipt(receipt: CustomerReceipt): Observable<CustomerReceipt> {
    return this.http
      .post<CustomerReceipt | ApiResponse<CustomerReceipt>>(this.baseUrl, receipt)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateReceipt(id: number, receipt: CustomerReceipt): Observable<CustomerReceipt> {
    return this.http
      .put<CustomerReceipt | ApiResponse<CustomerReceipt>>(`${this.baseUrl}/${id}`, receipt)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  postReceipt(id: number): Observable<CustomerReceipt> {
    return this.http
      .post<CustomerReceipt | ApiResponse<CustomerReceipt>>(`${this.baseUrl}/${id}/post`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  cancelReceipt(id: number): Observable<CustomerReceipt> {
    return this.http
      .post<CustomerReceipt | ApiResponse<CustomerReceipt>>(`${this.baseUrl}/${id}/cancel`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
