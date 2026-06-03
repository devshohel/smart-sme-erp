import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Customer } from '../models/customer.model';
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

  searchCustomers(keyword: string): Observable<Customer[]> {
    const params = keyword.trim()
      ? new HttpParams().set('keyword', keyword.trim())
      : undefined;

    return this.http
      .get<Customer[] | ApiResponse<Customer[]>>(`${this.baseUrl}/search`, { params })
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
