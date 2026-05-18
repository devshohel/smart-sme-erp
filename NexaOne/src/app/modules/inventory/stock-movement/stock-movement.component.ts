import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Product } from '../../../models/product.model';
import { StockMovement, MovementType } from '../../../models/stock-movement.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { ProductService } from '../../../services/product.service';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { debugApiError } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-stock-movement',
  templateUrl: './stock-movement.component.html',
  styleUrls: ['./stock-movement.component.css']
})
export class StockMovementComponent implements OnInit {
  movements: StockMovement[] = [];
  filteredMovements: StockMovement[] = [];
  products: Product[] = [];
  warehouses: InventoryWarehouse[] = [];
  loading = false;

  filters = {
    productId: '',
    warehouseId: '',
    movementType: ''
  };

  readonly movementTypes: MovementType[] = ['IN', 'OUT', 'ADJUSTMENT', 'TRANSFER'];

  constructor(
    private stockService: InventoryStockService,
    private productService: ProductService,
    private warehouseService: InventoryWarehouseService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.filters.productId = params.get('productId') || '';
      this.filters.warehouseId = params.get('warehouseId') || '';
      this.loadMovements();
    });

    this.loadOptions();
  }

  loadOptions(): void {
    this.productService.getAllProducts().subscribe({
      next: (data) => this.products = data,
      error: (error) => {
        this.products = [];
        debugApiError('StockMovementComponent.loadProducts', error);
      }
    });

    this.warehouseService.getAllWarehouses().subscribe({
      next: (data) => this.warehouses = data,
      error: (error) => {
        this.warehouses = [];
        debugApiError('StockMovementComponent.loadWarehouses', error);
      }
    });
  }

  loadMovements(): void {
    this.loading = true;
    this.stockService.getAllMovements().subscribe({
      next: (data) => {
        this.movements = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.movements = [];
        this.filteredMovements = [];
        this.loading = false;
        debugApiError('StockMovementComponent.loadMovements', error);
      }
    });
  }

  applyFilters(): void {
    this.filteredMovements = this.movements.filter(movement => {
      const matchesProduct = !this.filters.productId || String(movement.productId) === this.filters.productId;
      const matchesWarehouse = !this.filters.warehouseId || String(movement.warehouseId) === this.filters.warehouseId;
      const matchesType = !this.filters.movementType || movement.movementType === this.filters.movementType;

      return matchesProduct && matchesWarehouse && matchesType;
    });
  }

  resetFilters(): void {
    this.filters = {
      productId: '',
      warehouseId: '',
      movementType: ''
    };
    this.applyFilters();
  }
}
