import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { PurchaseOrder, PurchaseReceive, PurchaseReturn } from '../models/purchase.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class PurchaseService {
  private readonly ordersUrl = `${environment.apiUrl}/purchases/orders`;
  private readonly receivesUrl = `${environment.apiUrl}/purchases/receives`;
  private readonly returnsUrl = `${environment.apiUrl}/purchases/returns`;
  private readonly ordersSubject = new BehaviorSubject<PurchaseOrder[]>([]);
  private readonly returnsSubject = new BehaviorSubject<PurchaseReturn[]>([]);

  constructor(private http: HttpClient) {}

  getAllOrders(): Observable<PurchaseOrder[]> {
    return this.http
      .get<PurchaseOrder[] | ApiResponse<PurchaseOrder[]>>(this.ordersUrl)
      .pipe(
        map(response => unwrapApiResponse(response).map(order => this.normalizeOrder(order))),
        tap(orders => this.ordersSubject.next(orders))
      );
  }

  getOrderById(id: number): Observable<PurchaseOrder> {
    return this.http
      .get<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${id}`)
      .pipe(
        map(response => this.normalizeOrder(unwrapApiResponse(response)))
      );
  }

  saveOrder(order: PurchaseOrder): Observable<PurchaseOrder> {
    const payload = this.normalizeOrder(order);
    const request$ = payload.id
      ? this.http.put<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${payload.id}`, payload)
      : this.http.post<PurchaseOrder | ApiResponse<PurchaseOrder>>(this.ordersUrl, payload);

    return request$.pipe(
      map(response => this.normalizeOrder(unwrapApiResponse(response))),
      tap(saved => this.upsertOrder(saved))
    );
  }

  submitOrder(id: number): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${id}/submit`, {}).pipe(
      map(response => this.normalizeOrder(unwrapApiResponse(response))),
      tap(saved => this.upsertOrder(saved))
    );
  }

  approveOrder(id: number): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${id}/approve`, {}).pipe(
      map(response => this.normalizeOrder(unwrapApiResponse(response))),
      tap(saved => this.upsertOrder(saved))
    );
  }

  rejectOrder(id: number, reason: string): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${id}/reject`, { reason }).pipe(
      map(response => this.normalizeOrder(unwrapApiResponse(response))),
      tap(saved => this.upsertOrder(saved))
    );
  }

  cancelOrder(id: number): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${id}/cancel`, {}).pipe(
      map(response => this.normalizeOrder(unwrapApiResponse(response))),
      tap(saved => this.upsertOrder(saved))
    );
  }

  receiveOrder(id: number, receive: PurchaseReceive): Observable<PurchaseOrder> {
    const payload = this.normalizeReceive(receive);
    return this.http.post<PurchaseOrder | ApiResponse<PurchaseOrder>>(`${this.ordersUrl}/${id}/receive`, payload).pipe(
      map(response => this.normalizeOrder(unwrapApiResponse(response))),
      tap(saved => this.upsertOrder(saved))
    );
  }

  getAllReceives(): Observable<PurchaseReceive[]> {
    return this.http
      .get<PurchaseReceive[] | ApiResponse<PurchaseReceive[]>>(this.receivesUrl)
      .pipe(map(response => unwrapApiResponse(response).map(item => this.normalizeReceive(item))));
  }

  getAllReturns(): Observable<PurchaseReturn[]> {
    return this.http
      .get<PurchaseReturn[] | ApiResponse<PurchaseReturn[]>>(this.returnsUrl)
      .pipe(
        map(response => unwrapApiResponse(response).map(item => this.normalizeReturn(item))),
        tap(items => this.returnsSubject.next(items))
      );
  }

  getReturnById(id: number): Observable<PurchaseReturn> {
    return this.http
      .get<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${id}`)
      .pipe(
        map(response => this.normalizeReturn(unwrapApiResponse(response)))
      );
  }

  saveReturn(purchaseReturn: PurchaseReturn): Observable<PurchaseReturn> {
    const payload = this.normalizeReturn(purchaseReturn);
    const request$ = payload.id
      ? this.http.put<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${payload.id}`, payload)
      : this.http.post<PurchaseReturn | ApiResponse<PurchaseReturn>>(this.returnsUrl, payload);

    return request$
      .pipe(
        map(response => this.normalizeReturn(unwrapApiResponse(response))),
        tap(saved => this.upsertReturn(saved))
      );
  }

  submitReturn(id: number): Observable<PurchaseReturn> {
    return this.http.post<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${id}/submit`, {}).pipe(
      map(response => this.normalizeReturn(unwrapApiResponse(response))),
      tap(saved => this.upsertReturn(saved))
    );
  }

  approveReturn(id: number): Observable<PurchaseReturn> {
    return this.http.post<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${id}/approve`, {}).pipe(
      map(response => this.normalizeReturn(unwrapApiResponse(response))),
      tap(saved => this.upsertReturn(saved))
    );
  }

  rejectReturn(id: number, reason: string): Observable<PurchaseReturn> {
    return this.http.post<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${id}/reject`, { reason }).pipe(
      map(response => this.normalizeReturn(unwrapApiResponse(response))),
      tap(saved => this.upsertReturn(saved))
    );
  }

  postReturn(id: number): Observable<PurchaseReturn> {
    return this.http.post<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${id}/post`, {}).pipe(
      map(response => this.normalizeReturn(unwrapApiResponse(response))),
      tap(saved => this.upsertReturn(saved))
    );
  }

  cancelReturn(id: number): Observable<PurchaseReturn> {
    return this.http.post<PurchaseReturn | ApiResponse<PurchaseReturn>>(`${this.returnsUrl}/${id}/cancel`, {}).pipe(
      map(response => this.normalizeReturn(unwrapApiResponse(response))),
      tap(saved => this.upsertReturn(saved))
    );
  }

  private upsertOrder(order: PurchaseOrder): void {
    const orders = [...this.ordersSubject.value];
    const index = orders.findIndex(item => item.id === order.id);
    if (index >= 0) {
      orders[index] = order;
    } else {
      orders.unshift(order);
    }
    this.ordersSubject.next(orders);
  }

  private upsertReturn(purchaseReturn: PurchaseReturn): void {
    const returns = [...this.returnsSubject.value];
    const index = returns.findIndex(item => item.id === purchaseReturn.id);
    if (index >= 0) {
      returns[index] = purchaseReturn;
    } else {
      returns.unshift(purchaseReturn);
    }
    this.returnsSubject.next(returns);
  }

  private normalizeOrder(order: PurchaseOrder): PurchaseOrder {
    return {
      ...order,
      supplierId: order.supplierId ?? null,
      warehouseId: order.warehouseId ?? null,
      purchaseDate: this.toApiDateTime(order.purchaseDate),
      totalAmount: Number(order.totalAmount || 0),
      discountAmount: Number(order.discountAmount || 0),
      taxAmount: Number(order.taxAmount || 0),
      netTotal: Number(order.netTotal || 0),
      paidAmount: Number(order.paidAmount || 0),
      dueAmount: Number(order.dueAmount || 0),
      status: order.status || 'PENDING',
      items: (order.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        uomId: item.uomId ?? null,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        discount: Number(item.discount || 0),
        tax: Number(item.tax || 0),
        subTotal: Number(item.subTotal || 0),
        receivedQuantity: Number(item.receivedQuantity || 0),
        returnedQuantity: Number(item.returnedQuantity || 0)
      }))
    };
  }

  private normalizeReceive(receive: PurchaseReceive): PurchaseReceive {
    return {
      ...receive,
      purchaseOrderId: receive.purchaseOrderId ?? null,
      warehouseId: receive.warehouseId ?? null,
      receiveDate: this.toApiDateTime(receive.receiveDate),
      items: (receive.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        orderedQty: Number(item.orderedQty || 0),
        receivedQty: Number(item.receivedQty || 0),
        remainingQty: Number(item.remainingQty || 0)
      }))
    };
  }

  private normalizeReturn(purchaseReturn: PurchaseReturn): PurchaseReturn {
    return {
      ...purchaseReturn,
      purchaseId: purchaseReturn.purchaseId ?? null,
      supplierId: purchaseReturn.supplierId ?? null,
      returnDate: this.toApiDateTime(purchaseReturn.returnDate),
      totalAmount: Number(purchaseReturn.totalAmount || 0),
      status: purchaseReturn.status || 'DRAFT',
      items: (purchaseReturn.items || []).map(item => ({
        ...item,
        productId: item.productId ?? null,
        quantity: Number(item.quantity || 0),
        unitPrice: Number(item.unitPrice || 0),
        total: Number(item.total || 0)
      }))
    };
  }

  private toApiDateTime(value: string): string {
    return value && value.length === 10 ? `${value}T00:00:00` : value;
  }
}
