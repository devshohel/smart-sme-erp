import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Status } from '../../../models/product.model';
import { Supplier } from '../../../models/supplier.model';
import { SupplierService } from '../../../services/supplier.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-supplier-list',
  templateUrl: './supplier-list.component.html',
  styleUrls: ['./supplier-list.component.css']
})
export class SupplierListComponent implements OnInit {
  suppliers: Supplier[] = [];
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    status: '' as Status | ''
  };

  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];

  constructor(
    private supplierService: SupplierService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.supplierService.getAllSuppliers(this.filters.keyword, this.filters.status).subscribe({
      next: (suppliers) => {
        this.suppliers = suppliers;
        this.loading = false;
      },
      error: (error) => {
        this.suppliers = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Suppliers could not be loaded.');
        debugApiError('SupplierListComponent.loadSuppliers', error);
      }
    });
  }

  search(): void {
    this.loadSuppliers();
  }

  resetFilters(): void {
    this.filters = {
      keyword: '',
      status: ''
    };
    this.loadSuppliers();
  }

  createSupplier(): void {
    this.router.navigate(['/suppliers/create']);
  }

  editSupplier(supplier: Supplier): void {
    if (supplier.id) {
      this.router.navigate(['/suppliers/edit', supplier.id]);
    }
  }

  deleteSupplier(supplier: Supplier): void {
    if (!supplier.id) {
      return;
    }

    if (confirm(`Are you sure you want to delete supplier "${supplier.name}"?`)) {
      this.supplierService.deleteSupplier(supplier.id).subscribe({
        next: () => this.loadSuppliers(),
        error: (error) => {
          this.errorMessage = extractApiErrorMessage(error, 'Delete request failed.');
          debugApiError('SupplierListComponent.deleteSupplier', error);
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
