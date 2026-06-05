import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-sales-status-badge',
  templateUrl: './sales-status-badge.component.html',
  styleUrls: ['./sales-status-badge.component.css']
})
export class SalesStatusBadgeComponent {
  @Input() label = '';
  @Input() type: 'order' | 'payment' = 'order';

  get badgeClass(): string {
    const value = this.label || '';

    if (this.type === 'payment') {
      switch (value) {
        case 'PAID':
          return 'bg-success-subtle text-success';
        case 'PARTIAL':
          return 'bg-warning-subtle text-warning';
        default:
          return 'bg-danger-subtle text-danger';
      }
    }

    switch (value) {
      case 'DRAFT':
        return 'bg-secondary-subtle text-secondary';
      case 'APPROVED':
      case 'CONFIRMED':
        return 'bg-success-subtle text-success';
      case 'COMPLETED':
        return 'bg-info-subtle text-info';
      case 'CANCELLED':
        return 'bg-danger-subtle text-danger';
      default:
        return 'bg-warning-subtle text-warning';
    }
  }
}
