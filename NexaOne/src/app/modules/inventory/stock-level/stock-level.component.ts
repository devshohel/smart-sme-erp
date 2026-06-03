import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Product } from '../../../models/product.model';
import { Stock } from '../../../models/stock.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { ProductService } from '../../../services/product.service';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-stock-level',
  templateUrl: './stock-level.component.html',
  styleUrls: ['./stock-level.component.css']
})
export class StockLevelComponent implements OnInit {
  filterForm: FormGroup;
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  results: Stock[] = [];
  loading = false;
  errorMessage = '';
  searchKeyword = '';

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private stockService: InventoryStockService,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      productId: [null, Validators.required],
      warehouseId: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadOptions();
    this.loadAllStock();
  }

  loadOptions(): void {
    this.productService.getAllProducts().subscribe({
      next: (data) => this.products = data,
      error: (error) => {
        this.products = [];
        debugApiError('StockLevelComponent.loadProducts', error);
      }
    });

    this.warehouseService.getAllWarehouses().subscribe({
      next: (data) => this.warehouses = data,
      error: (error) => {
        this.warehouses = [];
        debugApiError('StockLevelComponent.loadWarehouses', error);
      }
    });
  }

  loadAllStock(): void {
    this.loading = true;
    this.errorMessage = '';

    this.stockService.getAllStock().subscribe({
      next: (data) => {
        this.results = data;
        this.loading = false;
      },
      error: (error) => {
        this.results = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Current stock could not be loaded.');
        debugApiError('StockLevelComponent.loadAllStock', error);
      }
    });
  }

  lookupStock(): void {
    this.errorMessage = '';
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }

    const value = this.filterForm.value;
    this.loading = true;

    this.stockService.getStock(Number(value.productId), Number(value.warehouseId)).subscribe({
      next: (stock) => {
        this.loading = false;
        this.upsertResult(stock);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Current stock could not be found for the selected product and warehouse.');
        debugApiError('StockLevelComponent.lookupStock', error);
      }
    });
  }

  upsertResult(stock: Stock): void {
    const index = this.results.findIndex(item => item.productId === stock.productId && item.warehouseId === stock.warehouseId);
    if (index >= 0) {
      this.results[index] = stock;
    } else {
      this.results.unshift(stock);
    }
  }

  filteredResults(): Stock[] {
    const keyword = this.searchKeyword.trim().toLowerCase();
    if (!keyword) {
      return this.results;
    }

    return this.results.filter(stock =>
      (stock.productName || '').toLowerCase().includes(keyword) ||
      (stock.warehouseName || '').toLowerCase().includes(keyword)
    );
  }

  isLowStock(stock: Stock): boolean {
    const reorderLevel = stock.reorderLevel ?? 0;
    return reorderLevel > 0 && stock.quantity <= reorderLevel;
  }

  viewMovements(stock: Stock): void {
    this.router.navigate(['/inventory/movements'], {
      queryParams: {
        productId: stock.productId,
        warehouseId: stock.warehouseId
      }
    });
  }

  hasError(controlName: string): boolean {
    const control = this.filterForm.get(controlName);
    return !!control && control.touched && control.invalid;
  }
}
