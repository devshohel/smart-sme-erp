import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { StockCard, StockMovement, StockMovementPage, StockMovementSearchParams } from '../models/stock-movement.model';
import { Stock, StockPage, StockSearchParams } from '../models/stock.model';
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

  getStockPage(params: StockSearchParams): Observable<StockPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10))
      .set('sort', params.sort || 'productName')
      .set('direction', params.direction || 'asc');

    if (params.keyword) {
      httpParams = httpParams.set('keyword', params.keyword);
    }
    if (params.warehouseId) {
      httpParams = httpParams.set('warehouseId', String(params.warehouseId));
    }
    if (params.categoryId) {
      httpParams = httpParams.set('categoryId', String(params.categoryId));
    }
    if (params.lowStockOnly) {
      httpParams = httpParams.set('lowStockOnly', 'true');
    }

    return this.http
      .get<StockPage | ApiResponse<StockPage>>(`${this.stocksUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getAllMovements(): Observable<StockMovement[]> {
    return this.http
      .get<StockMovement[] | ApiResponse<StockMovement[]>>(this.movementsUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getMovementPage(params: StockMovementSearchParams): Observable<StockMovementPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10))
      .set('sort', params.sort || 'createdAt')
      .set('direction', params.direction || 'desc');

    if (params.keyword) {
      httpParams = httpParams.set('keyword', params.keyword);
    }
    if (params.productId) {
      httpParams = httpParams.set('productId', String(params.productId));
    }
    if (params.warehouseId) {
      httpParams = httpParams.set('warehouseId', String(params.warehouseId));
    }
    if (params.movementType) {
      httpParams = httpParams.set('movementType', String(params.movementType));
    }
    if (params.referenceType) {
      httpParams = httpParams.set('referenceType', params.referenceType);
    }
    if (params.fromDate) {
      httpParams = httpParams.set('fromDate', params.fromDate);
    }
    if (params.toDate) {
      httpParams = httpParams.set('toDate', params.toDate);
    }

    return this.http
      .get<StockMovementPage | ApiResponse<StockMovementPage>>(`${this.movementsUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getStockCard(productId: number, warehouseId: number): Observable<StockCard> {
    const params = new HttpParams()
      .set('productId', productId.toString())
      .set('warehouseId', warehouseId.toString());

    return this.http
      .get<StockCard | ApiResponse<StockCard>>(`${this.stocksUrl}/card`, { params })
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
