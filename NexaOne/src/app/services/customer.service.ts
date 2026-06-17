import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  Customer,
  CustomerAgingReport,
  CustomerDetail,
  CustomerLedger,
  CustomerOption,
  CustomerPage,
  CustomerSearchParams
} from '../models/customer.model';
import { Status } from '../models/product.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly baseUrl = `${environment.apiUrl}/customers`;

  constructor(private http: HttpClient) {}

  getAllCustomers(keyword?: string, status?: Status | ''): Observable<Customer[]> {
    let params = new HttpParams();

    if (keyword?.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    if (status) {
      params = params.set('status', status);
    }

    return this.http
      .get<Customer[] | ApiResponse<Customer[]>>(this.baseUrl, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCustomerPage(params: CustomerSearchParams): Observable<CustomerPage> {
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
      .get<CustomerPage | ApiResponse<CustomerPage>>(`${this.baseUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  searchCustomers(keyword: string): Observable<CustomerOption[]> {
    const params = keyword.trim()
      ? new HttpParams().set('keyword', keyword.trim())
      : undefined;

    return this.http
      .get<CustomerOption[] | ApiResponse<CustomerOption[]>>(`${this.baseUrl}/search`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCustomerDetail(id: number): Observable<CustomerDetail> {
    return this.http
      .get<CustomerDetail | ApiResponse<CustomerDetail>>(`${this.baseUrl}/${id}/detail`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCustomerLedger(id: number, fromDate?: string, toDate?: string): Observable<CustomerLedger> {
    let params = new HttpParams();
    if (fromDate) {
      params = params.set('fromDate', fromDate);
    }
    if (toDate) {
      params = params.set('toDate', toDate);
    }

    return this.http
      .get<CustomerLedger | ApiResponse<CustomerLedger>>(`${this.baseUrl}/${id}/ledger`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCustomerAging(customerId?: number | null, fromDate?: string, toDate?: string): Observable<CustomerAgingReport> {
    let params = new HttpParams();
    if (customerId) {
      params = params.set('customerId', String(customerId));
    }
    if (fromDate) {
      params = params.set('fromDate', fromDate);
    }
    if (toDate) {
      params = params.set('toDate', toDate);
    }

    return this.http
      .get<CustomerAgingReport | ApiResponse<CustomerAgingReport>>(`${this.baseUrl}/aging`, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getCustomerById(id: number): Observable<Customer> {
    return this.http
      .get<Customer | ApiResponse<Customer>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  createCustomer(customer: Customer): Observable<Customer> {
    return this.http
      .post<Customer | ApiResponse<Customer>>(this.baseUrl, customer)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateCustomer(id: number, customer: Customer): Observable<Customer> {
    return this.http
      .put<Customer | ApiResponse<Customer>>(`${this.baseUrl}/${id}`, customer)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
