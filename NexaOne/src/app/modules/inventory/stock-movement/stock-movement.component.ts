import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Product } from '../../../models/product.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { MovementType, StockMovement } from '../../../models/stock-movement.model';
import { ProductService } from '../../../services/product.service';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

type SortDirection = 'asc' | 'desc';

interface MovementFilters {
  keyword: string;
  productId: number | null;
  warehouseId: number | null;
  movementType: MovementType | '';
  referenceType: string;
  fromDate: string;
  toDate: string;
}

@Component({
  selector: 'app-stock-movement',
  templateUrl: './stock-movement.component.html',
  styleUrls: ['./stock-movement.component.css']
})
export class StockMovementComponent implements OnInit {
  movements: StockMovement[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  loading = false;
  errorMessage = '';

  filters: MovementFilters = {
    keyword: '',
    productId: null,
    warehouseId: null,
    movementType: '',
    referenceType: '',
    fromDate: '',
    toDate: ''
  };

  page = 0;
  size = 10;
  readonly pageSizes = [10, 25, 50, 100];
  totalElements = 0;
  totalPages = 0;
  sort = 'createdAt';
  direction: SortDirection = 'desc';

  readonly movementTypes: MovementType[] = ['IN', 'OUT', 'ADJUSTMENT', 'TRANSFER'];

  constructor(
    private stockService: InventoryStockService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.route.queryParamMap.subscribe(params => {
      this.filters.productId = this.toNumberOrNull(params.get('productId'));
      this.filters.warehouseId = this.toNumberOrNull(params.get('warehouseId'));
      this.page = 0;
      this.loadMovements();
    });
  }

  loadOptions(): void {
    this.productService.getAllProducts().subscribe({
      next: data => this.products = data,
      error: error => debugApiError('StockMovementComponent.loadProducts', error)
    });
    this.warehouseService.getAllWarehouses().subscribe({
      next: data => this.warehouses = data,
      error: error => debugApiError('StockMovementComponent.loadWarehouses', error)
    });
  }

  loadMovements(): void {
    this.loading = true;
    this.errorMessage = '';

    this.stockService.getMovementPage({
      keyword: this.filters.keyword,
      productId: this.filters.productId,
      warehouseId: this.filters.warehouseId,
      movementType: this.filters.movementType,
      referenceType: this.filters.referenceType,
      fromDate: this.filters.fromDate,
      toDate: this.filters.toDate,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    }).subscribe({
      next: data => {
        this.movements = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.page = data.page;
        this.size = data.size;
        this.loading = false;
      },
      error: error => {
        this.movements = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Stock movements could not be loaded.');
        debugApiError('StockMovementComponent.loadMovements', error);
      }
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadMovements();
  }

  resetFilters(): void {
    this.filters = {
      keyword: '',
      productId: null,
      warehouseId: null,
      movementType: '',
      referenceType: '',
      fromDate: '',
      toDate: ''
    };
    this.page = 0;
    this.loadMovements();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadMovements();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = column === 'createdAt' ? 'desc' : 'asc';
    }
    this.loadMovements();
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
    this.loadMovements();
  }

  movementBadgeClass(type?: string): string {
    switch (type) {
      case 'IN': return 'bg-success-subtle text-success';
      case 'OUT': return 'bg-danger-subtle text-danger';
      case 'ADJUSTMENT': return 'bg-warning-subtle text-warning';
      default: return 'bg-secondary-subtle text-secondary';
    }
  }

  private toNumberOrNull(value: string | null): number | null {
    if (!value) {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }
}
