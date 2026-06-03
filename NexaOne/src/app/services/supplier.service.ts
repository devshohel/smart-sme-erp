import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Status } from '../models/product.model';
import { Supplier } from '../models/supplier.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class SupplierService {
  private readonly baseUrl = `${environment.apiUrl}/suppliers`;

  constructor(private http: HttpClient) {}

  getAllSuppliers(keyword?: string, status?: Status | ''): Observable<Supplier[]> {
    let params = new HttpParams();

    if (keyword?.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    if (status) {
      params = params.set('status', status);
    }

    return this.http
      .get<Supplier[] | ApiResponse<Supplier[]>>(this.baseUrl, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  searchSuppliers(keyword: string): Observable<Supplier[]> {
    const params = keyword.trim()
      ? new HttpParams().set('keyword', keyword.trim())
      : undefined;

    return this.http
      .get<Supplier[] | ApiResponse<Supplier[]>>(`${this.baseUrl}/search`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getSupplierById(id: number): Observable<Supplier> {
    return this.http
      .get<Supplier | ApiResponse<Supplier>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  createSupplier(supplier: Supplier): Observable<Supplier> {
    return this.http
      .post<Supplier | ApiResponse<Supplier>>(this.baseUrl, supplier)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateSupplier(id: number, supplier: Supplier): Observable<Supplier> {
    return this.http
      .put<Supplier | ApiResponse<Supplier>>(`${this.baseUrl}/${id}`, supplier)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteSupplier(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
