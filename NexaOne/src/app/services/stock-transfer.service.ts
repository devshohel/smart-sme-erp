import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { StockTransfer, StockTransferPage, StockTransferSearchParams } from '../models/stock-transfer.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class StockTransferService {
  private baseUrl = `${environment.apiUrl}/inventory/transfers`;

  constructor(private http: HttpClient) {}

  getPage(params: StockTransferSearchParams): Observable<StockTransferPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10))
      .set('sort', params.sort || 'id')
      .set('direction', params.direction || 'desc');

    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.fromWarehouseId) httpParams = httpParams.set('fromWarehouseId', String(params.fromWarehouseId));
    if (params.toWarehouseId) httpParams = httpParams.set('toWarehouseId', String(params.toWarehouseId));
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.fromDate) httpParams = httpParams.set('fromDate', params.fromDate);
    if (params.toDate) httpParams = httpParams.set('toDate', params.toDate);

    return this.http.get<StockTransferPage | ApiResponse<StockTransferPage>>(`${this.baseUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getById(id: number): Observable<StockTransfer> {
    return this.http.get<StockTransfer | ApiResponse<StockTransfer>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  create(transfer: StockTransfer): Observable<StockTransfer> {
    return this.http.post<StockTransfer | ApiResponse<StockTransfer>>(this.baseUrl, transfer)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  update(id: number, transfer: StockTransfer): Observable<StockTransfer> {
    return this.http.put<StockTransfer | ApiResponse<StockTransfer>>(`${this.baseUrl}/${id}`, transfer)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  approve(id: number): Observable<StockTransfer> { return this.action(id, 'approve'); }
  send(id: number): Observable<StockTransfer> { return this.action(id, 'send'); }
  receive(id: number): Observable<StockTransfer> { return this.action(id, 'receive'); }
  cancel(id: number): Observable<StockTransfer> { return this.action(id, 'cancel'); }

  private action(id: number, action: string): Observable<StockTransfer> {
    return this.http.post<StockTransfer | ApiResponse<StockTransfer>>(`${this.baseUrl}/${id}/${action}`, null)
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
