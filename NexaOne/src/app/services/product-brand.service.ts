import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Brand } from '../models/brand.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class ProductBrandService {

  private baseUrl = `${environment.apiUrl}/brands`;

  constructor(private http: HttpClient) {}

  getAllBrands(): Observable<Brand[]> {
    return this.http
      .get<Brand[] | ApiResponse<Brand[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getBrandById(id: number): Observable<Brand> {
    return this.http
      .get<Brand | ApiResponse<Brand>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveBrand(brand: Brand): Observable<Brand> {
    return this.http
      .post<Brand | ApiResponse<Brand>>(this.baseUrl, brand)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteBrand(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
