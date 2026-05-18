import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product } from '../models/product.model';
import { environment } from '../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private baseUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  // Get All
  getAllProducts(): Observable<Product[]> {
    return this.http
      .get<Product[] | ApiResponse<Product[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getProductById(id: number): Observable<Product> {
    return this.http
      .get<Product | ApiResponse<Product>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveProduct(product: Product): Observable<Product> {
    return this.http
      .post<Product | ApiResponse<Product>>(this.baseUrl, product)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  createProduct(product: Product): Observable<Product> {
    return this.saveProduct(product);
  }

  // Delete (Soft Delete)
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
