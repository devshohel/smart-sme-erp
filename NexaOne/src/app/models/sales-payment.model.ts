import { PaymentStatus } from './sales-common.model';

/** Shared UI payment model. API-specific names are mapped at the integration boundary. */
export type SalesPaymentMethod = 'CASH' | 'CARD' | 'BANK' | 'MOBILE' | 'CREDIT';

export interface SalesPaymentInput {
  paidAmount: number;
  paymentMethod: SalesPaymentMethod;
  reference?: string;
  notes?: string;
}

export interface ApiPaymentRequest {
  paymentMethod: 'CASH' | 'CARD' | 'BANK' | 'MOBILE_BANKING' | 'DUE';
  paidAmount: number;
  referenceNo?: string;
}

export const SALES_PAYMENT_METHODS: ReadonlyArray<{ value: SalesPaymentMethod; label: string }> = [
  { value: 'CASH', label: 'Cash' },
  { value: 'CARD', label: 'Card' },
  { value: 'BANK', label: 'Bank' },
  { value: 'MOBILE', label: 'Mobile' },
  { value: 'CREDIT', label: 'Credit' }
];

export function normalizeSalesPayment(input: SalesPaymentInput): SalesPaymentInput {
  const amount = Number(input.paidAmount);
  return {
    paidAmount: input.paymentMethod === 'CREDIT' ? 0 : (Number.isFinite(amount) ? Math.max(amount, 0) : 0),
    paymentMethod: input.paymentMethod,
    reference: input.reference?.trim() || undefined,
    notes: input.notes?.trim() || undefined
  };
}

/** Validation preview only. The persisted payment status always comes from the API response. */
export function previewPaymentStatus(input: SalesPaymentInput, previewTotal: number): PaymentStatus {
  const payment = normalizeSalesPayment(input);
  if (payment.paidAmount <= 0) return 'DUE';
  return payment.paidAmount >= Number(previewTotal || 0) ? 'PAID' : 'PARTIAL';
}

export function toApiPayment(input: SalesPaymentInput): ApiPaymentRequest {
  const payment = normalizeSalesPayment(input);
  const methodMap: Record<SalesPaymentMethod, ApiPaymentRequest['paymentMethod']> = {
    CASH: 'CASH',
    CARD: 'CARD',
    BANK: 'BANK',
    MOBILE: 'MOBILE_BANKING',
    CREDIT: 'DUE'
  };
  return {
    paymentMethod: methodMap[payment.paymentMethod],
    paidAmount: payment.paidAmount,
    referenceNo: payment.reference
  };
}

export function toReceiptPaymentMethod(method: SalesPaymentMethod): 'CASH' | 'BANK' | 'MOBILE_BANKING' {
  if (method === 'BANK' || method === 'CARD') return 'BANK';
  if (method === 'MOBILE') return 'MOBILE_BANKING';
  return 'CASH';
}
