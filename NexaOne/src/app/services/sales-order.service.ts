import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
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

    return this.http
      .post<SalesOrder | ApiResponse<SalesOrder>>(this.baseUrl, payload)
      .pipe(map(response => this.normalizeOrder(unwrapApiResponse(response))));
  }

  private normalizeOrder(order: SalesOrder): SalesOrder {
    return {
      ...order,
      customerId: order.customerId ?? null,
      warehouseId: order.warehouseId ?? null,
      orderDate: this.toApiDateTime(order.orderDate),
      status: order.status || 'PENDING',
      grandTotal: Number(order.grandTotal || 0),
      items: (order.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        subtotal: Number(item.subtotal || 0)
      }))
    };
  }

  private toApiDateTime(value: string): string {
    return value && value.length === 10 ? `${value}T00:00:00` : value;
  }
}
