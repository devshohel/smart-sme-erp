import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Status } from '../models/product.model';
import { Supplier, SupplierAgingReport, SupplierDetail, SupplierLedger, SupplierOption, SupplierPage } from '../models/supplier.model';
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

  getSupplierPage(params: {
    keyword?: string;
    status?: Status | '';
    page?: number;
    size?: number;
    sort?: string;
    direction?: 'asc' | 'desc';
  }): Observable<SupplierPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10))
      .set('sort', params.sort || 'createdAt')
      .set('direction', params.direction || 'desc');

    if (params.keyword?.trim()) {
      httpParams = httpParams.set('keyword', params.keyword.trim());
    }

    if (params.status) {
      httpParams = httpParams.set('status', params.status);
    }

    return this.http
      .get<SupplierPage | ApiResponse<SupplierPage>>(`${this.baseUrl}/page`, { params: httpParams })
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

  getSupplierOptions(keyword: string): Observable<SupplierOption[]> {
    const params = keyword.trim()
      ? new HttpParams().set('keyword', keyword.trim())
      : undefined;

    return this.http
      .get<SupplierOption[] | ApiResponse<SupplierOption[]>>(`${this.baseUrl}/options`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getSupplierById(id: number): Observable<Supplier> {
    return this.http
      .get<Supplier | ApiResponse<Supplier>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getSupplierDetail(id: number): Observable<SupplierDetail> {
    return this.http
      .get<SupplierDetail | ApiResponse<SupplierDetail>>(`${this.baseUrl}/${id}/detail`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getSupplierLedger(id: number, fromDate?: string, toDate?: string): Observable<SupplierLedger> {
    let params = new HttpParams();
    if (fromDate) {
      params = params.set('fromDate', fromDate);
    }
    if (toDate) {
      params = params.set('toDate', toDate);
    }

    return this.http
      .get<SupplierLedger | ApiResponse<SupplierLedger>>(`${this.baseUrl}/${id}/ledger`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getSupplierAging(supplierId?: number | null, fromDate?: string, toDate?: string): Observable<SupplierAgingReport> {
    let params = new HttpParams();
    if (supplierId) {
      params = params.set('supplierId', String(supplierId));
    }
    if (fromDate) {
      params = params.set('fromDate', fromDate);
    }
    if (toDate) {
      params = params.set('toDate', toDate);
    }

    return this.http
      .get<SupplierAgingReport | ApiResponse<SupplierAgingReport>>(`${this.baseUrl}/aging`, { params })
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
