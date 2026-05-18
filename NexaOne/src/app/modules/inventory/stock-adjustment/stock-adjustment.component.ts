import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Product } from '../../../models/product.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { ProductService } from '../../../services/product.service';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

function nonZeroNumberValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = Number(control.value);
    if (!control.value && control.value !== 0) {
      return null;
    }
    return value === 0 ? { nonZero: true } : null;
  };
}

@Component({
  selector: 'app-stock-adjustment',
  templateUrl: './stock-adjustment.component.html',
  styleUrls: ['./stock-adjustment.component.css']
})
export class StockAdjustmentComponent implements OnInit {
  adjustmentForm: FormGroup;
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private stockService: InventoryStockService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService
  ) {
    this.adjustmentForm = this.fb.group({
      productId: [null, Validators.required],
      warehouseId: [null, Validators.required],
      quantity: [null, [Validators.required, nonZeroNumberValidator()]],
      reason: ['', [Validators.required, Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.loadOptions();
  }

  loadOptions(): void {
    this.productService.getAllProducts().subscribe({
      next: (data) => this.products = data,
      error: (error) => {
        this.products = [];
        debugApiError('StockAdjustmentComponent.loadProducts', error);
      }
    });

    this.warehouseService.getAllWarehouses().subscribe({
      next: (data) => this.warehouses = data,
      error: (error) => {
        this.warehouses = [];
        debugApiError('StockAdjustmentComponent.loadWarehouses', error);
      }
    });
  }

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.adjustmentForm.invalid) {
      this.adjustmentForm.markAllAsTouched();
      return;
    }

    const value = this.adjustmentForm.value;
    this.isSubmitting = true;

    this.stockService.adjustStock(
      Number(value.productId),
      Number(value.warehouseId),
      Number(value.quantity),
      value.reason.trim()
    ).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Stock adjustment submitted successfully.';
        this.adjustmentForm.reset({
          productId: null,
          warehouseId: null,
          quantity: null,
          reason: ''
        });
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Stock adjustment could not be submitted.');
        debugApiError('StockAdjustmentComponent.submit', error);
      }
    });
  }

  reset(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.adjustmentForm.reset({
      productId: null,
      warehouseId: null,
      quantity: null,
      reason: ''
    });
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.adjustmentForm.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
  }
}
