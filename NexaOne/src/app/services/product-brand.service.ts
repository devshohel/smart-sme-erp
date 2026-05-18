import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Brand } from '../models/brand.model';

@Injectable({
  providedIn: 'root'
})
export class ProductBrandService {

  private baseUrl = `${environment.apiUrl}/brands`;

  constructor(private http: HttpClient) {}

  getAllBrands(): Observable<Brand[]> {
    return this.http.get<Brand[]>(this.baseUrl);
  }

  getBrandById(id: number): Observable<Brand> {
    return this.http.get<Brand>(`${this.baseUrl}/${id}`);
  }

  saveBrand(brand: Brand): Observable<Brand> {
    return this.http.post<Brand>(this.baseUrl, brand);
  }

  deleteBrand(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
