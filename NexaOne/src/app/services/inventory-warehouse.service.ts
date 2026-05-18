import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { InventoryWarehouse } from '../models/inventory-warehouse.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class InventoryWarehouseService {
  private baseUrl = `${environment.apiUrl}/warehouses`;

  constructor(private http: HttpClient) {}

  getAllWarehouses(): Observable<InventoryWarehouse[]> {
    return this.http
      .get<InventoryWarehouse[] | ApiResponse<InventoryWarehouse[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getWarehouseById(id: number): Observable<InventoryWarehouse> {
    return this.http
      .get<InventoryWarehouse | ApiResponse<InventoryWarehouse>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveWarehouse(warehouse: InventoryWarehouse): Observable<InventoryWarehouse> {
    return this.http
      .post<InventoryWarehouse | ApiResponse<InventoryWarehouse>>(this.baseUrl, warehouse)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteWarehouse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
