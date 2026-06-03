import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { SalesCustomer } from '../models/sales-common.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class SalesCustomerService {
  private readonly baseUrl = `${environment.apiUrl}/customers`;

  constructor(private http: HttpClient) {}

  getAllCustomers(): Observable<SalesCustomer[]> {
    return this.http
      .get<SalesCustomer[] | ApiResponse<SalesCustomer[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
