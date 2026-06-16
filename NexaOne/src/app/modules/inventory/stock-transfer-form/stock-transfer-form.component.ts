import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../models/product.model';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { StockTransfer, StockTransferItem, StockTransferStatus } from '../../../models/stock-transfer.model';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { ProductService } from '../../../services/product.service';
import { StockTransferService } from '../../../services/stock-transfer.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-stock-transfer-form',
  templateUrl: './stock-transfer-form.component.html',
  styleUrls: ['./stock-transfer-form.component.css']
})
export class StockTransferFormComponent implements OnInit {
  transferId: number | null = null;
  warehouses: InventoryWarehouse[] = [];
  products: Product[] = [];
  loading = false;
  submitting = false;
  errorMessage = '';

  transfer: StockTransfer = this.emptyTransfer();
  readonly createStatuses: StockTransferStatus[] = ['DRAFT', 'PENDING'];
  readonly editStatuses: StockTransferStatus[] = ['DRAFT', 'PENDING', 'APPROVED'];

  constructor(
    private transferService: StockTransferService,
    private warehouseService: InventoryWarehouseService,
    private productService: ProductService,
    private stockService: InventoryStockService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.transferId = Number(this.route.snapshot.paramMap.get('id')) || null;
    this.loadOptions();
    if (this.transferId) {
      this.loadTransfer(this.transferId);
    }
  }

  loadOptions(): void {
    this.warehouseService.getAllWarehouses().subscribe({
      next: data => this.warehouses = data,
      error: error => debugApiError('StockTransferFormComponent.loadWarehouses', error)
    });
    this.productService.getAllProducts().subscribe({
      next: data => this.products = data,
      error: error => debugApiError('StockTransferFormComponent.loadProducts', error)
    });
  }

  loadTransfer(id: number): void {
    this.loading = true;
    this.transferService.getById(id).subscribe({
      next: data => {
        this.transfer = data;
        this.transfer.items.forEach(item => this.loadCurrentStock(item));
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Transfer could not be loaded.');
      }
    });
  }

  addItem(): void {
    this.transfer.items.push({ productId: null, quantity: null, remarks: '', currentStock: null });
  }

  removeItem(index: number): void {
    if (this.transfer.items.length > 1) {
      this.transfer.items.splice(index, 1);
    }
  }

  onProductChange(item: StockTransferItem): void {
    const product = this.products.find(p => p.id === Number(item.productId));
    item.productName = product?.productName;
    item.sku = product?.sku;
    this.loadCurrentStock(item);
  }

  onWarehouseChange(): void {
    this.transfer.items.forEach(item => this.loadCurrentStock(item));
  }

  save(): void {
    this.errorMessage = '';
    if (!this.isValid()) {
      this.errorMessage = 'Please complete required fields and valid item quantities.';
      return;
    }
    this.submitting = true;
    const request = this.transferId
      ? this.transferService.update(this.transferId, this.transfer)
      : this.transferService.create(this.transfer);
    request.subscribe({
      next: saved => this.router.navigate(['/inventory/transfers/details', saved.id]),
      error: error => {
        this.submitting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Transfer could not be saved.');
        debugApiError('StockTransferFormComponent.save', error);
      }
    });
  }

  back(): void {
    this.router.navigate(['/inventory/transfers']);
  }

  isValid(): boolean {
    return !!this.transfer.fromWarehouseId
      && !!this.transfer.toWarehouseId
      && this.transfer.fromWarehouseId !== this.transfer.toWarehouseId
      && !!this.transfer.transferDate
      && this.transfer.items.length > 0
      && this.transfer.items.every(item => !!item.productId && Number(item.quantity) > 0);
  }

  statusOptions(): StockTransferStatus[] {
    return this.transferId ? this.editStatuses : this.createStatuses;
  }

  private loadCurrentStock(item: StockTransferItem): void {
    if (!this.transfer.fromWarehouseId || !item.productId) {
      item.currentStock = null;
      return;
    }
    this.stockService.getStock(Number(item.productId), Number(this.transfer.fromWarehouseId)).subscribe({
      next: stock => item.currentStock = stock.quantity,
      error: () => item.currentStock = 0
    });
  }

  private emptyTransfer(): StockTransfer {
    return {
      fromWarehouseId: null,
      toWarehouseId: null,
      status: 'DRAFT',
      transferDate: new Date().toISOString().slice(0, 10),
      expectedDate: null,
      remarks: '',
      items: [{ productId: null, quantity: null, remarks: '', currentStock: null }]
    };
  }
}
