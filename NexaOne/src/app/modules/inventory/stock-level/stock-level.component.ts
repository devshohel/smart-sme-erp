import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductCategory } from '../../../models/category.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { Stock } from '../../../models/stock.model';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductCategoryService } from '../../../services/product-category.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

type SortDirection = 'asc' | 'desc';

interface StockFilters {
  keyword: string;
  warehouseId: number | null;
  categoryId: number | null;
  lowStockOnly: boolean;
}

@Component({
  selector: 'app-stock-level',
  templateUrl: './stock-level.component.html',
  styleUrls: ['./stock-level.component.css']
})
export class StockLevelComponent implements OnInit {
  stocks: Stock[] = [];
  warehouses: InventoryWarehouse[] = [];
  categories: ProductCategory[] = [];
  filters: StockFilters = { keyword: '', warehouseId: null, categoryId: null, lowStockOnly: false };
  loading = false;
  errorMessage = '';

  page = 0;
  size = 10;
  readonly pageSizes = [10, 25, 50, 100];
  totalElements = 0;
  totalPages = 0;
  sort = 'productName';
  direction: SortDirection = 'asc';

  constructor(
    private stockService: InventoryStockService,
    private warehouseService: InventoryWarehouseService,
    private categoryService: ProductCategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.loadStock();
  }

  loadOptions(): void {
    this.warehouseService.getAllWarehouses().subscribe({
      next: data => this.warehouses = data,
      error: error => debugApiError('StockLevelComponent.loadWarehouses', error)
    });
    this.categoryService.getAllCategories().subscribe({
      next: data => this.categories = data,
      error: error => debugApiError('StockLevelComponent.loadCategories', error)
    });
  }

  loadStock(): void {
    this.loading = true;
    this.errorMessage = '';

    this.stockService.getStockPage({
      keyword: this.filters.keyword,
      warehouseId: this.filters.warehouseId,
      categoryId: this.filters.categoryId,
      lowStockOnly: this.filters.lowStockOnly,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    }).subscribe({
      next: data => {
        this.stocks = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.page = data.page;
        this.size = data.size;
        this.loading = false;
      },
      error: error => {
        this.stocks = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Current stock could not be loaded.');
        debugApiError('StockLevelComponent.loadStock', error);
      }
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadStock();
  }

  resetFilters(): void {
    this.filters = { keyword: '', warehouseId: null, categoryId: null, lowStockOnly: false };
    this.page = 0;
    this.loadStock();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadStock();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = 'asc';
    }
    this.loadStock();
  }

  sortIcon(column: string): string {
    if (this.sort !== column) {
      return 'bi-arrow-down-up';
    }
    return this.direction === 'asc' ? 'bi-sort-alpha-down' : 'bi-sort-alpha-up';
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) {
      return;
    }
    this.page = page;
    this.loadStock();
  }

  isLowStock(stock: Stock): boolean {
    const reorderLevel = stock.reorderLevel ?? 0;
    return reorderLevel > 0 && Number(stock.quantity || 0) <= reorderLevel;
  }

  viewMovements(stock: Stock): void {
    this.router.navigate(['/inventory/movements'], {
      queryParams: {
        productId: stock.productId,
        warehouseId: stock.warehouseId
      }
    });
  }

  viewStockCard(stock: Stock): void {
    this.router.navigate(['/inventory/stock-card'], {
      queryParams: {
        productId: stock.productId,
        warehouseId: stock.warehouseId
      }
    });
  }
}
