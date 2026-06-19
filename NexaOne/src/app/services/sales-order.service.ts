import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SalesInvoice } from '../models/sales-invoice.model';
import { SalesOrder } from '../models/sales-order.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class SalesOrderService {
  private readonly baseUrl = `${environment.apiUrl}/sales/orders`;

  constructor(private http: HttpClient) {}

  getAllOrders(): Observable<SalesOrder[]> {
    return this.http
      .get<SalesOrder[] | ApiResponse<SalesOrder[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response).map(order => this.normalizeOrder(order))));
  }

  saveOrder(order: SalesOrder): Observable<SalesOrder> {
    const payload = this.normalizeOrder(order);

    const request$ = payload.id
      ? this.http.put<SalesOrder | ApiResponse<SalesOrder>>(`${this.baseUrl}/${payload.id}`, payload)
      : this.http.post<SalesOrder | ApiResponse<SalesOrder>>(this.baseUrl, payload);

    return request$
      .pipe(map(response => this.normalizeOrder(unwrapApiResponse(response))));
  }

  submitOrder(id: number): Observable<SalesOrder> {
    return this.http.post<SalesOrder | ApiResponse<SalesOrder>>(`${this.baseUrl}/${id}/submit`, {})
      .pipe(map(response => this.normalizeOrder(unwrapApiResponse(response))));
  }

  approveOrder(id: number): Observable<SalesOrder> {
    return this.http.post<SalesOrder | ApiResponse<SalesOrder>>(`${this.baseUrl}/${id}/approve`, {})
      .pipe(map(response => this.normalizeOrder(unwrapApiResponse(response))));
  }

  rejectOrder(id: number, reason: string): Observable<SalesOrder> {
    return this.http.post<SalesOrder | ApiResponse<SalesOrder>>(`${this.baseUrl}/${id}/reject`, { reason })
      .pipe(map(response => this.normalizeOrder(unwrapApiResponse(response))));
  }

  cancelOrder(id: number): Observable<SalesOrder> {
    return this.http.post<SalesOrder | ApiResponse<SalesOrder>>(`${this.baseUrl}/${id}/cancel`, {})
      .pipe(map(response => this.normalizeOrder(unwrapApiResponse(response))));
  }

  convertOrderToInvoice(id: number): Observable<SalesInvoice> {
    return this.http.post<SalesInvoice | ApiResponse<SalesInvoice>>(`${this.baseUrl}/${id}/convert-to-invoice`, {})
      .pipe(map(response => unwrapApiResponse(response) as SalesInvoice));
  }

  private normalizeOrder(order: SalesOrder): SalesOrder {
    return {
      ...order,
      customerId: order.customerId ?? null,
      warehouseId: order.warehouseId ?? null,
      orderDate: this.toApiDateTime(order.orderDate),
      status: order.status || 'DRAFT',
      grandTotal: Number(order.grandTotal || 0),
      items: (order.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        uomId: item.uomId ?? null,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        subtotal: Number((item as any).subtotal ?? (item as any).subTotal ?? 0),
        subTotal: Number((item as any).subtotal ?? (item as any).subTotal ?? 0)
      }))
    };
  }

  private toApiDateTime(value: string): string {
    return value && value.length === 10 ? `${value}T00:00:00` : value;
  }
}
