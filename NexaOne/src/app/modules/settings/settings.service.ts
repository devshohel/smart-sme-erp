import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';
import { CompanySettings, InvoiceSettings, SalesFeatureSettings, SystemSettings, TaxSettings } from '../auth/auth.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly settingsUrl = `${environment.apiUrl}/settings`;
  private readonly defaultSalesFeatures: SalesFeatureSettings = {
    enableControlledSalesMode: false,
    enableSalesOrders: false,
    enableQuotations: false,
    enableDeliveryNotes: false,
    enableSalesApproval: false,
    enableManualAllocation: false,
    enableAdvancedInvoice: false
  };
  private readonly salesFeaturesSubject = new BehaviorSubject<SalesFeatureSettings>(this.defaultSalesFeatures);
  readonly salesFeatures$ = this.salesFeaturesSubject.asObservable();

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
      .pipe(
        map(response => unwrapApiResponse(response)),
        tap(settings => this.publishSalesFeatures(settings))
      );
  }

  updateSystem(settings: SystemSettings): Observable<SystemSettings> {
    return this.http
      .put<SystemSettings | ApiResponse<SystemSettings>>(`${this.settingsUrl}/system`, settings)
      .pipe(
        map(response => unwrapApiResponse(response)),
        tap(settings => this.publishSalesFeatures(settings))
      );
  }

  loadSalesFeatures(): Observable<SalesFeatureSettings> {
    return this.http
      .get<SalesFeatureSettings | ApiResponse<SalesFeatureSettings>>(`${this.settingsUrl}/sales-features`)
      .pipe(
        map(response => this.normalizeSalesFeatures(unwrapApiResponse(response))),
        tap(settings => this.salesFeaturesSubject.next(settings))
      );
  }

  currentSalesFeatures(): SalesFeatureSettings {
    return this.salesFeaturesSubject.value;
  }

  private publishSalesFeatures(settings: SystemSettings): void {
    this.salesFeaturesSubject.next(this.normalizeSalesFeatures(settings));
  }

  private normalizeSalesFeatures(settings: Partial<SalesFeatureSettings>): SalesFeatureSettings {
    return {
      enableControlledSalesMode: settings.enableControlledSalesMode === true,
      enableSalesOrders: settings.enableSalesOrders === true,
      enableQuotations: settings.enableQuotations === true,
      enableDeliveryNotes: settings.enableDeliveryNotes === true,
      enableSalesApproval: settings.enableSalesApproval === true,
      enableManualAllocation: settings.enableManualAllocation === true,
      enableAdvancedInvoice: settings.enableAdvancedInvoice === true
    };
  }
}
