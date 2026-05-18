import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Uom } from '../models/uom.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({
  providedIn: 'root'
})
export class UomService {

  private baseUrl = `${environment.apiUrl}/uoms`;

  constructor(private http: HttpClient) {}

  getAllUoms(): Observable<Uom[]> {
    return this.http
      .get<Uom[] | ApiResponse<Uom[]>>(this.baseUrl)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getUomById(id: number): Observable<Uom> {
    return this.http
      .get<Uom | ApiResponse<Uom>>(`${this.baseUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveUom(uom: Uom): Observable<Uom> {
    return this.http
      .post<Uom | ApiResponse<Uom>>(this.baseUrl, uom)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteUom(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
