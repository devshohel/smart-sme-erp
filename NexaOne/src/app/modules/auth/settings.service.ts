import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';
import { CompanySettings, InvoiceSettings, SystemSettings, TaxSettings } from './auth.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly settingsUrl = `${environment.apiUrl}/settings`;

  constructor(private http: HttpClient) {}

  getCompany(): Observable<CompanySettings> {
    return this.http
      .get<CompanySettings | ApiResponse<CompanySettings>>(`${this.settingsUrl}/company`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateCompany(settings: CompanySettings): Observable<CompanySettings> {
    return this.http
      .put<CompanySettings | ApiResponse<CompanySettings>>(`${this.settingsUrl}/company`, settings)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getInvoice(): Observable<InvoiceSettings> {
    return this.http
      .get<InvoiceSettings | ApiResponse<InvoiceSettings>>(`${this.settingsUrl}/invoice`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateInvoice(settings: InvoiceSettings): Observable<InvoiceSettings> {
    return this.http
      .put<InvoiceSettings | ApiResponse<InvoiceSettings>>(`${this.settingsUrl}/invoice`, settings)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getTax(): Observable<TaxSettings> {
    return this.http
      .get<TaxSettings | ApiResponse<TaxSettings>>(`${this.settingsUrl}/tax`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateTax(settings: TaxSettings): Observable<TaxSettings> {
    return this.http
      .put<TaxSettings | ApiResponse<TaxSettings>>(`${this.settingsUrl}/tax`, settings)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getSystem(): Observable<SystemSettings> {
    return this.http
      .get<SystemSettings | ApiResponse<SystemSettings>>(`${this.settingsUrl}/system`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateSystem(settings: SystemSettings): Observable<SystemSettings> {
    return this.http
      .put<SystemSettings | ApiResponse<SystemSettings>>(`${this.settingsUrl}/system`, settings)
      .pipe(map(response => unwrapApiResponse(response)));
  }
}
