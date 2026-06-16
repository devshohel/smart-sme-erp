import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StockTransfer, StockTransferStatus } from '../../../models/stock-transfer.model';
import { StockTransferService } from '../../../services/stock-transfer.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-stock-transfer-details',
  templateUrl: './stock-transfer-details.component.html',
  styleUrls: ['./stock-transfer-details.component.css']
})
export class StockTransferDetailsComponent implements OnInit {
  transfer: StockTransfer | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private transferService: StockTransferService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) this.loadTransfer(id);
  }

  loadTransfer(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.transferService.getById(id).subscribe({
      next: data => {
        this.transfer = data;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Transfer could not be loaded.');
        debugApiError('StockTransferDetailsComponent.loadTransfer', error);
      }
    });
  }

  back(): void { this.router.navigate(['/inventory/transfers']); }
  edit(): void { if (this.transfer?.id) this.router.navigate(['/inventory/transfers/edit', this.transfer.id]); }

  approve(): void { this.runAction('approve'); }
  send(): void { this.runAction('send'); }
  receive(): void { this.runAction('receive'); }
  cancel(): void {
    if (confirm('Cancel this stock transfer?')) this.runAction('cancel');
  }

  canEdit(status: StockTransferStatus): boolean { return ['DRAFT', 'PENDING', 'APPROVED'].includes(status); }
  canApprove(status: StockTransferStatus): boolean { return status === 'DRAFT' || status === 'PENDING'; }

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

  get totalQuantity(): number {
    return this.transfer?.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) || 0;
  }

  private runAction(action: 'approve' | 'send' | 'receive' | 'cancel'): void {
    if (!this.transfer?.id) return;
    this.loading = true;
    this.transferService[action](this.transfer.id).subscribe({
      next: data => {
        this.transfer = data;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Transfer action failed.');
        debugApiError(`StockTransferDetailsComponent.${action}`, error);
      }
    });
  }
}
