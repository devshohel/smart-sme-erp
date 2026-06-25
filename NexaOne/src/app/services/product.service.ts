import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product, ProductPage, ProductSearchParams, ProductStats, Status } from '../models/product.model';
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

  getDeletedProducts(): Observable<Product[]> {
    return this.http
      .get<Product[] | ApiResponse<Product[]>>(`${this.baseUrl}/deleted`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getProductPage(params: ProductSearchParams): Observable<ProductPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page))
      .set('size', String(params.size))
      .set('sort', params.sort)
      .set('direction', params.direction);
    if (params.keyword?.trim()) {
      httpParams = httpParams.set('keyword', params.keyword.trim());
    }
    if (params.categoryId) {
      httpParams = httpParams.set('categoryId', String(params.categoryId));
    }
    if (params.brandId) {
      httpParams = httpParams.set('brandId', String(params.brandId));
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status);
    }
    return this.http
      .get<ProductPage | ApiResponse<ProductPage>>(`${this.baseUrl}/page`, { params: httpParams })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getProductStats(): Observable<ProductStats> {
    return this.http
      .get<ProductStats | ApiResponse<ProductStats>>(`${this.baseUrl}/stats`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateStatusBulk(productIds: number[], status: Status): Observable<number> {
    return this.http
      .put<number | ApiResponse<number>>(`${this.baseUrl}/bulk-status`, { productIds, status })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getProductById(id: number): Observable<Product> {
    return this.http
      .get<Product | ApiResponse<Product>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveProduct(product: Product): Observable<Product> {
    const request$ = product.id
      ? this.http.put<Product | ApiResponse<Product>>(`${this.baseUrl}/${product.id}`, product)
      : this.http.post<Product | ApiResponse<Product>>(this.baseUrl, product);
    return request$.pipe(map(response => unwrapApiResponse(response)));
  }

  createProduct(product: Product): Observable<Product> {
    return this.http
      .post<Product | ApiResponse<Product>>(this.baseUrl, product)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  createProductMultipart(product: Product, imageFile?: File | null): Observable<Product> {
    return this.http
      .post<Product | ApiResponse<Product>>(this.baseUrl, this.buildProductFormData(product, imageFile))
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateProductMultipart(id: number, product: Product, imageFile?: File | null): Observable<Product> {
    return this.http
      .put<Product | ApiResponse<Product>>(`${this.baseUrl}/${id}`, this.buildProductFormData(product, imageFile))
      .pipe(map(response => unwrapApiResponse(response)));
  }

  // Delete (Soft Delete)
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  restoreProduct(id: number): Observable<Product> {
    return this.http
      .put<Product | ApiResponse<Product>>(`${this.baseUrl}/${id}/restore`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  resolveImageUrl(imageUrl?: string): string {
    if (!imageUrl) {
      return '';
    }
    if (/^https?:\/\//i.test(imageUrl)) {
      return imageUrl;
    }
    const apiRoot = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
    return `${apiRoot}${imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`}`;
  }

  private buildProductFormData(product: Product, imageFile?: File | null): FormData {
    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(product)], { type: 'application/json' }));
    if (imageFile) {
      formData.append('image', imageFile);
    }
    return formData;
  }
}
