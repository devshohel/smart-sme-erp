import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerReceipt } from '../../../../models/customer-receipt.model';
import { CustomerReceiptService } from '../../../../services/customer-receipt.service';
import { AuthService } from '../../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../../shared/utils/api-error.util';

@Component({
  selector: 'app-customer-receipt-details',
  templateUrl: './customer-receipt-details.component.html',
  styleUrls: ['./customer-receipt-details.component.css']
})
export class CustomerReceiptDetailsComponent implements OnInit {
  receipt: CustomerReceipt | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private receiptService: CustomerReceiptService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Receipt id is missing.';
      return;
    }
    this.loadReceipt(id);
  }

  loadReceipt(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.receiptService.getReceiptById(id).subscribe({
      next: receipt => {
        this.receipt = receipt;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Receipt details could not be loaded.');
        debugApiError('CustomerReceiptDetailsComponent.loadReceipt', error);
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/customers/receipts']);
  }

  editReceipt(): void {
    if (this.receipt?.id && this.receipt.status === 'DRAFT') {
      this.router.navigate(['/customers/receipts/edit', this.receipt.id]);
    }
  }

  postReceipt(): void {
    if (!this.receipt?.id || this.receipt.status !== 'DRAFT') {
      return;
    }
    this.receiptService.postReceipt(this.receipt.id).subscribe({
      next: receipt => this.receipt = receipt,
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Receipt could not be posted.');
        debugApiError('CustomerReceiptDetailsComponent.postReceipt', error);
      }
    });
  }

  cancelReceipt(): void {
    if (!this.receipt?.id || this.receipt.status !== 'DRAFT') {
      return;
    }
    if (!confirm(`Cancel receipt "${this.receipt.receiptNo || this.receipt.id}"?`)) {
      return;
    }
    this.receiptService.cancelReceipt(this.receipt.id).subscribe({
      next: receipt => this.receipt = receipt,
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Receipt could not be cancelled.');
        debugApiError('CustomerReceiptDetailsComponent.cancelReceipt', error);
      }
    });
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }
}
