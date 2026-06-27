import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SalesInvoice } from '../models/sales-invoice.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class SalesInvoiceService {
  private readonly baseUrl = `${environment.apiUrl}/sales/invoices`;

  constructor(private http: HttpClient) {}

  getAllInvoices(): Observable<SalesInvoice[]> {
    return this.http
      .get<SalesInvoice[] | ApiResponse<SalesInvoice[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response).map(invoice => this.normalizeInvoice(invoice))));
  }

  saveInvoice(invoice: SalesInvoice): Observable<SalesInvoice> {
    const payload = this.normalizeInvoice(invoice);

    const request$ = payload.id
      ? this.http.put<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${payload.id}`, payload)
      : this.http.post<SalesInvoice | ApiResponse<SalesInvoice>>(this.baseUrl, payload);

    return request$
      .pipe(map(response => this.normalizeInvoice(unwrapApiResponse(response))));
  }

  getInvoiceById(id: number): Observable<SalesInvoice> {
    return this.http
      .get<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => this.normalizeInvoice(unwrapApiResponse(response))));
  }

  submitInvoice(id: number): Observable<SalesInvoice> {
    return this.http.post<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${id}/submit`, {})
      .pipe(map(response => this.normalizeInvoice(unwrapApiResponse(response))));
  }

  approveInvoice(id: number): Observable<SalesInvoice> {
    return this.http.post<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${id}/approve`, {})
      .pipe(map(response => this.normalizeInvoice(unwrapApiResponse(response))));
  }

  postInvoice(id: number): Observable<SalesInvoice> {
    return this.http.post<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${id}/post`, {})
      .pipe(map(response => this.normalizeInvoice(unwrapApiResponse(response))));
  }

  cancelInvoice(id: number): Observable<SalesInvoice> {
    return this.http.post<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${id}/cancel`, {})
      .pipe(map(response => this.normalizeInvoice(unwrapApiResponse(response))));
  }

  private normalizeInvoice(invoice: SalesInvoice): SalesInvoice {
    return {
      ...invoice,
      orderId: invoice.orderId ?? null,
      customerId: invoice.customerId ?? null,
      warehouseId: invoice.warehouseId ?? null,
      saleDate: this.toApiDateTime(invoice.saleDate),
      totalAmount: Number(invoice.totalAmount || 0),
      discountAmount: Number(invoice.discountAmount || 0),
      taxAmount: Number(invoice.taxAmount || 0),
      netTotal: Number(invoice.netTotal || 0),
      paidAmount: Number(invoice.paidAmount || 0),
      dueAmount: Number(invoice.dueAmount || 0),
      paymentStatus: invoice.paymentStatus || 'DUE',
      status: invoice.status || 'DRAFT',
      items: (invoice.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        uomId: item.uomId ?? null,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        discount: Number(item.discount || 0),
        tax: Number(item.tax || 0),
        subtotal: Number((item as any).subtotal ?? (item as any).subTotal ?? 0),
        subTotal: Number((item as any).subtotal ?? (item as any).subTotal ?? 0)
      }))
    };
  }

  private toApiDateTime(value: string): string {
    return value && value.length === 10 ? `${value}T00:00:00` : value;
  }
}
