import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../../../models/product.model'; // পাথটি আপনার ফোল্ডার স্ট্রাকচার অনুযায়ী চেক করে নিন

@Injectable({ providedIn: 'root' })
export class ProductService {
  // আপনার ব্যাকএন্ডে v1 আছে, তাই ইউআরএলটি এমন হওয়া উচিত
  private apiUrl = 'http://localhost:8080/api/v1/products'; 

  constructor(private http: HttpClient) { }

  // সব প্রডাক্ট দেখার জন্য
  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  // নতুন প্রডাক্ট সেভ করার জন্য (এটি আপনার কম্পোনেন্টে দরকার)
  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  // প্রডাক্ট ডিলিট করার জন্য (এটিও আপনার কম্পোনেন্টে দরকার)
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}