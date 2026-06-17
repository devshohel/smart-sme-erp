import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerDetail, CustomerTransaction } from '../../../models/customer.model';
import { Status } from '../../../models/product.model';
import { CustomerService } from '../../../services/customer.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-customer-details',
  templateUrl: './customer-details.component.html',
  styleUrls: ['./customer-details.component.css']
})
export class CustomerDetailsComponent implements OnInit {
  detail: CustomerDetail | null = null;
  loading = false;
  errorMessage = '';
  activeTab: 'invoices' | 'returns' | 'ledger' | 'notes' = 'invoices';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Customer id is missing.';
      return;
    }
    this.loadCustomerDetail(id);
  }

  loadCustomerDetail(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.customerService.getCustomerDetail(id).subscribe({
      next: detail => {
        this.detail = detail;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Customer details could not be loaded.');
        debugApiError('CustomerDetailsComponent.loadCustomerDetail', error);
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/customers/list']);
  }

  editCustomer(): void {
    if (this.detail?.customer.id) {
      this.router.navigate(['/customers/edit', this.detail.customer.id]);
    }
  }

  statusClass(status?: Status): string {
    if (status === 'ACTIVE') {
      return 'active';
    }
    if (status === 'INACTIVE') {
      return 'inactive';
    }
    return 'neutral';
  }

  balanceStatusClass(status?: string): string {
    if (status === 'Over Limit') {
      return 'over';
    }
    if (status === 'Near Credit Limit') {
      return 'near';
    }
    return 'normal';
  }

  setActiveTab(tab: 'invoices' | 'returns' | 'ledger' | 'notes'): void {
    this.activeTab = tab;
  }

  trackTransaction(_: number, transaction: CustomerTransaction): number {
    return transaction.id;
  }
}
