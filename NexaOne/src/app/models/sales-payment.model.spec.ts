import { normalizeSalesPayment, previewPaymentStatus, toApiPayment, toReceiptPaymentMethod } from './sales-payment.model';

describe('sales payment mapping', () => {
  it('maps mobile payment to existing API contracts', () => {
    const input = { paidAmount: 50, paymentMethod: 'MOBILE' as const, reference: ' TX-1 ' };
    expect(toApiPayment(input)).toEqual({ paymentMethod: 'MOBILE_BANKING', paidAmount: 50, referenceNo: 'TX-1' });
    expect(toReceiptPaymentMethod(input.paymentMethod)).toBe('MOBILE_BANKING');
  });

  it('treats credit as an unpaid sale', () => {
    const input = { paidAmount: 100, paymentMethod: 'CREDIT' as const };
    expect(normalizeSalesPayment(input).paidAmount).toBe(0);
    expect(toApiPayment(input).paymentMethod).toBe('DUE');
    expect(previewPaymentStatus(input, 100)).toBe('DUE');
  });

  it('provides validation previews without calculating persisted due', () => {
    expect(previewPaymentStatus({ paidAmount: 40, paymentMethod: 'CASH' }, 100)).toBe('PARTIAL');
    expect(previewPaymentStatus({ paidAmount: 100, paymentMethod: 'BANK' }, 100)).toBe('PAID');
  });
});
