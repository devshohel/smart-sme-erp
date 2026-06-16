import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StockCard } from '../../../models/stock-movement.model';
import { InventoryStockService } from '../../../services/inventory-stock.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-stock-card',
  templateUrl: './stock-card.component.html',
  styleUrls: ['./stock-card.component.css']
})
export class StockCardComponent implements OnInit {
  stockCard: StockCard | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private stockService: InventoryStockService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const productId = Number(params.get('productId'));
      const warehouseId = Number(params.get('warehouseId'));
      if (!productId || !warehouseId) {
        this.errorMessage = 'Product and warehouse are required to view stock card.';
        return;
      }
      this.loadStockCard(productId, warehouseId);
    });
  }

  loadStockCard(productId: number, warehouseId: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.stockService.getStockCard(productId, warehouseId).subscribe({
      next: data => {
        this.stockCard = data;
        this.loading = false;
      },
      error: error => {
        this.stockCard = null;
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Stock card could not be loaded.');
        debugApiError('StockCardComponent.loadStockCard', error);
      }
    });
  }

  backToStock(): void {
    this.router.navigate(['/inventory/stocks']);
  }

  movementBadgeClass(type?: string): string {
    switch (type) {
      case 'IN': return 'bg-success-subtle text-success';
      case 'OUT': return 'bg-danger-subtle text-danger';
      case 'ADJUSTMENT': return 'bg-warning-subtle text-warning';
      default: return 'bg-secondary-subtle text-secondary';
    }
  }
}
