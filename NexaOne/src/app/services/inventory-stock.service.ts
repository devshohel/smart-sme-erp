import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { StockMovement } from '../models/stock-movement.model';
import { Stock } from '../models/stock.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class InventoryStockService {
  private stocksUrl = `${environment.apiUrl}/stocks`;
  private movementsUrl = `${environment.apiUrl}/movements`;
  private adjustmentsUrl = `${environment.apiUrl}/adjustments`;

  constructor(private http: HttpClient) {}

  getStock(productId: number, warehouseId: number): Observable<Stock> {
    const params = new HttpParams()
      .set('productId', productId.toString())
      .set('warehouseId', warehouseId.toString());

    return this.http
      .get<Stock | ApiResponse<Stock>>(this.stocksUrl, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getAllStock(): Observable<Stock[]> {
    return this.http
      .get<Stock[] | ApiResponse<Stock[]>>(`${this.stocksUrl}/all`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getAllMovements(): Observable<StockMovement[]> {
    return this.http
      .get<StockMovement[] | ApiResponse<StockMovement[]>>(this.movementsUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  adjustStock(productId: number, warehouseId: number, qty: number, reason: string): Observable<Stock> {
    const params = new HttpParams()
      .set('productId', productId.toString())
      .set('warehouseId', warehouseId.toString())
      .set('qty', qty.toString())
      .set('reason', reason);

    return this.http
      .post<Stock | ApiResponse<Stock>>(this.adjustmentsUrl, null, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
