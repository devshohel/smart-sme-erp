import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

declare var bootstrap: any;

@Component({
  selector: 'app-warehouse',
  templateUrl: './warehouse.component.html',
  styleUrls: ['./warehouse.component.css']
})
export class WarehouseComponent implements OnInit {
  warehouses: InventoryWarehouse[] = [];
  warehouseForm: FormGroup;
  loading = false;
  isEditMode = false;
  submitError = '';

  constructor(
    private warehouseService: InventoryWarehouseService,
    private fb: FormBuilder
  ) {
    this.warehouseForm = this.fb.group({
      id: [null],
      code: ['', [Validators.required, Validators.maxLength(50)]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      location: ['', [Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(1000)]],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadWarehouses();
  }

  loadWarehouses(): void {
    this.loading = true;
    this.warehouseService.getAllWarehouses().subscribe({
      next: (data) => {
        this.warehouses = data;
        this.loading = false;
      },
      error: (error) => {
        this.warehouses = [];
        this.loading = false;
        debugApiError('WarehouseComponent.loadWarehouses', error);
      }
    });
  }

  addWarehouse(): void {
    this.isEditMode = false;
    this.submitError = '';
    this.warehouseForm.reset({
      id: null,
      code: '',
      name: '',
      location: '',
      description: '',
      active: true
    });
    this.openModal();
  }

  editWarehouse(warehouse: InventoryWarehouse): void {
    this.isEditMode = true;
    this.submitError = '';
    this.warehouseForm.reset({
      id: warehouse.id || null,
      code: warehouse.code,
      name: warehouse.name,
      location: warehouse.location || '',
      description: warehouse.description || '',
      active: warehouse.active ?? true
    });
    this.openModal();
  }

  saveWarehouse(): void {
    if (this.warehouseForm.invalid) {
      this.warehouseForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.submitError = '';

    this.warehouseService.saveWarehouse(this.warehouseForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.closeModal();
        this.loadWarehouses();
      },
      error: (error) => {
        this.loading = false;
        this.submitError = extractApiErrorMessage(error, 'Warehouse could not be saved.');
        debugApiError('WarehouseComponent.saveWarehouse', error);
      }
    });
  }

  deleteWarehouse(id?: number): void {
    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this warehouse?')) {
      this.warehouseService.deleteWarehouse(id).subscribe(() => this.loadWarehouses());
    }
  }

  openModal(): void {
    const modal = new bootstrap.Modal(document.getElementById('warehouseModal'));
    modal.show();
  }

  closeModal(): void {
    const modalEl = document.getElementById('warehouseModal');
    const instance = bootstrap.Modal.getInstance(modalEl);
    if (instance) {
      instance.hide();
    }
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.warehouseForm.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
  }
}
