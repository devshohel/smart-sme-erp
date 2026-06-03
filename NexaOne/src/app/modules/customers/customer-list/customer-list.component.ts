import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Customer } from '../../../models/customer.model';
import { Status } from '../../../models/product.model';
import { CustomerService } from '../../../services/customer.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.css']
})
export class CustomerListComponent implements OnInit {
  customers: Customer[] = [];
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    status: '' as Status | ''
  };

  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];

  constructor(
    private customerService: CustomerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.customerService.getAllCustomers(this.filters.keyword, this.filters.status).subscribe({
      next: (customers) => {
        this.customers = customers;
        this.loading = false;
      },
      error: (error) => {
        this.customers = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Customers could not be loaded.');
        debugApiError('CustomerListComponent.loadCustomers', error);
      }
    });
  }

  search(): void {
    this.loadCustomers();
  }

  resetFilters(): void {
    this.filters = {
      keyword: '',
      status: ''
    };
    this.loadCustomers();
  }

  createCustomer(): void {
    this.router.navigate(['/customers/create']);
  }

  editCustomer(customer: Customer): void {
    if (customer.id) {
      this.router.navigate(['/customers/edit', customer.id]);
    }
  }

  deleteCustomer(customer: Customer): void {
    if (!customer.id) {
      return;
    }

    if (confirm(`Are you sure you want to delete customer "${customer.name}"?`)) {
      this.customerService.deleteCustomer(customer.id).subscribe({
        next: () => this.loadCustomers(),
        error: (error) => {
          this.errorMessage = extractApiErrorMessage(error, 'Delete request failed.');
          debugApiError('CustomerListComponent.deleteCustomer', error);
        }
      });
    }
  }

  statusClass(status?: Status): string {
    if (status === 'ACTIVE') {
      return 'bg-success-subtle text-success';
    }
    if (status === 'INACTIVE') {
      return 'bg-secondary-subtle text-secondary';
    }
    return 'bg-light text-dark';
  }
}
