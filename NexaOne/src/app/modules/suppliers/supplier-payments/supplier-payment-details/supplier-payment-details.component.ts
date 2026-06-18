import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SupplierPayment } from '../../../../models/supplier-payment.model';
import { SupplierPaymentService } from '../../../../services/supplier-payment.service';
import { debugApiError, extractApiErrorMessage } from '../../../../shared/utils/api-error.util';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-supplier-payment-details',
  templateUrl: './supplier-payment-details.component.html',
  styleUrls: ['./supplier-payment-details.component.css']
})
export class SupplierPaymentDetailsComponent implements OnInit {
  payment: SupplierPayment | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: SupplierPaymentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Supplier payment id is missing.';
      return;
    }
    this.loadPayment(id);
  }

  loadPayment(id: number): void {
    this.loading = true;
    this.paymentService.getPaymentById(id).subscribe({
      next: payment => {
        this.payment = payment;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be loaded.');
        debugApiError('SupplierPaymentDetailsComponent.loadPayment', error);
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/suppliers/payments']);
  }

  editPayment(): void {
    if (this.payment?.id && this.payment.status === 'DRAFT') {
      this.router.navigate(['/suppliers/payments/edit', this.payment.id]);
    }
  }

  postPayment(): void {
    if (!this.payment?.id || this.payment.status !== 'DRAFT') {
      return;
    }
    this.paymentService.postPayment(this.payment.id).subscribe({
      next: payment => this.payment = payment,
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be posted.');
        debugApiError('SupplierPaymentDetailsComponent.postPayment', error);
      }
    });
  }

  reversePayment(): void {
    if (!this.payment?.id || this.payment.status !== 'POSTED' || !this.payment.canReverse) {
      return;
    }
    const reason = prompt(`Reverse supplier payment "${this.payment.paymentNo || this.payment.id}"? Enter reversal reason:`);
    if (reason === null) {
      return;
    }
    this.paymentService.reversePayment(this.payment.id, reason).subscribe({
      next: payment => this.payment = payment,
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Supplier payment could not be reversed.');
        debugApiError('SupplierPaymentDetailsComponent.reversePayment', error);
      }
    });
  }

  print(): void {
    window.print();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }
}
