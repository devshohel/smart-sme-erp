import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { InventoryWarehouse } from '../../../models/inventory-warehouse.model';
import { StockTransfer, StockTransferStatus } from '../../../models/stock-transfer.model';
import { InventoryWarehouseService } from '../../../services/inventory-warehouse.service';
import { StockTransferService } from '../../../services/stock-transfer.service';
import { AuthService } from '../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-stock-transfer-list',
  templateUrl: './stock-transfer-list.component.html',
  styleUrls: ['./stock-transfer-list.component.css']
})
export class StockTransferListComponent implements OnInit {
  transfers: StockTransfer[] = [];
  warehouses: InventoryWarehouse[] = [];
  loading = false;
  errorMessage = '';

  filters = {
    keyword: '',
    fromWarehouseId: null as number | null,
    toWarehouseId: null as number | null,
    status: '' as StockTransferStatus | '',
    fromDate: '',
    toDate: ''
  };

  page = 0;
  size = 10;
  readonly pageSizes = [10, 25, 50, 100];
  totalElements = 0;
  totalPages = 0;
  sort = 'id';
  direction: SortDirection = 'desc';
  readonly statuses: StockTransferStatus[] = ['DRAFT', 'PENDING', 'APPROVED', 'IN_TRANSIT', 'RECEIVED', 'CANCELLED'];

  constructor(
    private transferService: StockTransferService,
    private warehouseService: InventoryWarehouseService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadWarehouses();
    this.loadTransfers();
  }

  loadWarehouses(): void {
    this.warehouseService.getAllWarehouses().subscribe({
      next: data => this.warehouses = data,
      error: error => debugApiError('StockTransferListComponent.loadWarehouses', error)
    });
  }

  loadTransfers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.transferService.getPage({
      ...this.filters,
      page: this.page,
      size: this.size,
      sort: this.sort,
      direction: this.direction
    }).subscribe({
      next: data => {
        this.transfers = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.page = data.page;
        this.size = data.size;
        this.loading = false;
      },
      error: error => {
        this.transfers = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Stock transfers could not be loaded.');
        debugApiError('StockTransferListComponent.loadTransfers', error);
      }
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadTransfers();
  }

  resetFilters(): void {
    this.filters = { keyword: '', fromWarehouseId: null, toWarehouseId: null, status: '', fromDate: '', toDate: '' };
    this.page = 0;
    this.loadTransfers();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadTransfers();
  }

  setSort(column: string): void {
    if (this.sort === column) {
      this.direction = this.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sort = column;
      this.direction = column === 'transferDate' ? 'desc' : 'asc';
    }
    this.loadTransfers();
  }

  sortIcon(column: string): string {
    if (this.sort !== column) return 'bi-arrow-down-up';
    return this.direction === 'asc' ? 'bi-sort-alpha-down' : 'bi-sort-alpha-up';
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.page) return;
    this.page = page;
    this.loadTransfers();
  }

  createTransfer(): void {
    this.router.navigate(['/inventory/transfers/create']);
  }

  view(transfer: StockTransfer): void {
    this.router.navigate(['/inventory/transfers/details', transfer.id]);
  }

  edit(transfer: StockTransfer): void {
    this.router.navigate(['/inventory/transfers/edit', transfer.id]);
  }

  approve(transfer: StockTransfer): void { this.runAction(transfer, 'approve'); }
  send(transfer: StockTransfer): void { this.runAction(transfer, 'send'); }
  receive(transfer: StockTransfer): void { this.runAction(transfer, 'receive'); }
  cancel(transfer: StockTransfer): void {
    if (confirm('Cancel this stock transfer?')) {
      this.runAction(transfer, 'cancel');
    }
  }

  canEdit(status: StockTransferStatus): boolean {
    return this.hasPermission('TRANSFER_EDIT') && ['DRAFT', 'PENDING', 'APPROVED'].includes(status);
  }

  canApprove(status: StockTransferStatus): boolean {
    return this.hasPermission('TRANSFER_APPROVE') && (status === 'DRAFT' || status === 'PENDING');
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  statusClass(status: StockTransferStatus): string {
    switch (status) {
      case 'DRAFT': return 'bg-secondary-subtle text-secondary';
      case 'PENDING': return 'bg-warning-subtle text-warning';
      case 'APPROVED': return 'bg-primary-subtle text-primary';
      case 'IN_TRANSIT': return 'bg-info-subtle text-info';
      case 'RECEIVED': return 'bg-success-subtle text-success';
      case 'CANCELLED': return 'bg-danger-subtle text-danger';
    }
  }

  private runAction(transfer: StockTransfer, action: 'approve' | 'send' | 'receive' | 'cancel'): void {
    if (!transfer.id) return;
    this.loading = true;
    this.transferService[action](transfer.id).subscribe({
      next: () => this.loadTransfers(),
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Transfer action failed.');
        debugApiError(`StockTransferListComponent.${action}`, error);
      }
    });
  }
}
