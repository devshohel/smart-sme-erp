import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProductCategory } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class ProductCategoryService {

  private baseUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<ProductCategory[]> {
    return this.http.get<ProductCategory[]>(this.baseUrl);
  }

  saveCategory(category: ProductCategory): Observable<ProductCategory> {
    return this.http.post<ProductCategory>(this.baseUrl, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
