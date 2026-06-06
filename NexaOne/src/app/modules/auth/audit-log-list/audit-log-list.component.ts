import { Component, OnInit } from '@angular/core';
import { AuditFilter, AuditLog } from '../auth.model';
import { AuthService } from '../auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

type AuditData = Record<string, unknown>;

interface AuditChange {
  field: string;
  oldValue: string;
  newValue: string;
}

@Component({
  selector: 'app-audit-log-list',
  templateUrl: './audit-log-list.component.html'
})
export class AuditLogListComponent implements OnInit {
  logs: AuditLog[] = [];
  filter: AuditFilter = this.emptyFilter();
  selectedLog: AuditLog | null = null;
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.getAuditLogs(this.filter).subscribe({
      next: logs => {
        this.logs = logs;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Audit logs could not be loaded.');
        debugApiError('AuditLogListComponent.load', error);
      }
    });
  }

  reset(): void {
    this.filter = this.emptyFilter();
    this.load();
  }

  openDetails(log: AuditLog): void {
    this.selectedLog = log;
  }

  closeDetails(): void {
    this.selectedLog = null;
  }

  summary(log: AuditLog): string {
    const data = this.primaryData(log);
    const tableLabel = this.entityLabel(log.tableName);
    const recordName = this.recordLabel(log, data);
    const action = this.actionLabel(log.action);
    return recordName ? `${action} ${tableLabel}: ${recordName}` : `${action} ${tableLabel}`;
  }

  badgeClass(action: string): string {
    const normalized = action.toUpperCase();
    if (normalized === 'CREATE') {
      return 'bg-success';
    }
    if (normalized === 'UPDATE') {
      return 'bg-primary';
    }
    if (normalized === 'DELETE') {
      return 'bg-danger';
    }
    return 'bg-secondary';
  }

  detailRows(log: AuditLog): AuditChange[] {
    const action = log.action.toUpperCase();
    if (action === 'UPDATE') {
      return this.changedRows(this.parseData(log.oldData), this.parseData(log.newData));
    }
    const data = action === 'DELETE' ? this.parseData(log.oldData) : this.parseData(log.newData);
    return Object.entries(data)
      .filter(([key]) => this.isDisplayField(key))
      .map(([key, value]) => ({
        field: this.fieldLabel(key),
        oldValue: action === 'DELETE' ? this.formatValue(value) : '-',
        newValue: action === 'CREATE' ? this.formatValue(value) : '-'
      }));
  }

  isUpdate(log: AuditLog): boolean {
    return log.action.toUpperCase() === 'UPDATE';
  }

  truncate(value: string, length = 90): string {
    return value.length > length ? `${value.slice(0, length)}...` : value;
  }

  private emptyFilter(): AuditFilter {
    return { fromDate: '', toDate: '', username: '', action: '', module: '' };
  }

  private changedRows(oldData: AuditData, newData: AuditData): AuditChange[] {
    const keys = Array.from(new Set([...Object.keys(oldData), ...Object.keys(newData)]));
    return keys
      .filter(key => this.isDisplayField(key))
      .map(key => ({
        field: this.fieldLabel(key),
        oldValue: this.formatValue(oldData[key]),
        newValue: this.formatValue(newData[key])
      }))
      .filter(row => row.oldValue !== row.newValue);
  }

  private primaryData(log: AuditLog): AuditData {
    const newData = this.parseData(log.newData);
    return Object.keys(newData).length ? newData : this.parseData(log.oldData);
  }

  private parseData(value?: string | null): AuditData {
    if (!value) {
      return {};
    }
    try {
      const parsed = JSON.parse(value);
      return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
    } catch {
      return {};
    }
  }

  private recordLabel(log: AuditLog, data: AuditData): string {
    const candidates = [
      'productName',
      'name',
      'customerName',
      'supplierName',
      'orderNo',
      'purchaseCode',
      'invoiceNo',
      'username',
      'email',
      'code'
    ];
    const value = candidates.map(key => data[key]).find(candidate => candidate !== undefined && candidate !== null && `${candidate}`.trim());
    return value ? this.formatValue(value) : (log.recordId ? `#${log.recordId}` : '');
  }

  private entityLabel(tableName: string): string {
    const normalized = tableName.replace(/_/g, ' ').replace(/\s+/g, ' ').trim();
    return normalized
      .split(' ')
      .map(word => word ? `${word.charAt(0).toUpperCase()}${word.slice(1).toLowerCase()}` : '')
      .join(' ')
      .replace(/\bProducts\b/, 'Product')
      .replace(/\bCustomers\b/, 'Customer')
      .replace(/\bSuppliers\b/, 'Supplier')
      .replace(/\bUsers\b/, 'User')
      .replace(/\bPurchase Orders\b/, 'Purchase Order')
      .replace(/\bSales Orders\b/, 'Sales Order');
  }

  private actionLabel(action: string): string {
    const normalized = action.toUpperCase();
    if (normalized === 'CREATE') {
      return 'Created';
    }
    if (normalized === 'UPDATE') {
      return 'Updated';
    }
    if (normalized === 'DELETE') {
      return 'Deleted';
    }
    return this.fieldLabel(action.toLowerCase());
  }

  private fieldLabel(field: string): string {
    return field
      .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
      .replace(/_/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
      .split(' ')
      .map(word => word ? `${word.charAt(0).toUpperCase()}${word.slice(1)}` : '')
      .join(' ');
  }

  private formatValue(value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (Array.isArray(value)) {
      return `${value.length} item${value.length === 1 ? '' : 's'}`;
    }
    if (typeof value === 'object') {
      return JSON.stringify(value);
    }
    return `${value}`;
  }

  private isDisplayField(field: string): boolean {
    return !['id', 'password'].includes(field);
  }
}
