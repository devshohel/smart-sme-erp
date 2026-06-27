import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { SalesInvoiceLineItem } from '../models/sales-common.model';
import { SalesInvoice } from '../models/sales-invoice.model';

export type PosPaymentMethod = 'CASH' | 'CARD' | 'MOBILE_BANKING' | 'BANK' | 'DUE' | 'OTHER';

export interface PosSaleRequest {
  customerId: number | null;
  warehouseId: number | null;
  paymentMethod: PosPaymentMethod;
  paidAmount: number;
  discountAmount: number;
  items: SalesInvoiceLineItem[];
}

@Injectable({ providedIn: 'root' })
export class SalesPosService {
  /** Phase 3: enable only after an atomic backend POS completion endpoint exists. */
  readonly completionAvailable = false;

  completeSale(_request: PosSaleRequest): Observable<SalesInvoice> {
    return throwError(() => new Error('POS completion is not available until the Phase 3 backend workflow is implemented.'));
  }
}
