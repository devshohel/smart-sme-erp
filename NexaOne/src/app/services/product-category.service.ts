import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ProductCategory } from '../models/category.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class ProductCategoryService {

  private baseUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<ProductCategory[]> {
    return this.http
      .get<ProductCategory[] | ApiResponse<ProductCategory[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveCategory(category: ProductCategory): Observable<ProductCategory> {
    return this.http
      .post<ProductCategory | ApiResponse<ProductCategory>>(this.baseUrl, category)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
