import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Uom } from '../models/uom.model';

@Injectable({
  providedIn: 'root'
})
export class UomService {

  private baseUrl = `${environment.apiUrl}/uoms`;

  constructor(private http: HttpClient) {}

  getAllUoms(): Observable<Uom[]> {
    return this.http.get<Uom[]>(this.baseUrl);
  }

  getUomById(id: number): Observable<Uom> {
    return this.http.get<Uom>(`${this.baseUrl}/${id}`);
  }

  saveUom(uom: Uom): Observable<Uom> {
    return this.http.post<Uom>(this.baseUrl, uom);
  }

  deleteUom(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
