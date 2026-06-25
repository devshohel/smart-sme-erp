import { Component, OnInit } from '@angular/core';
import { ActivityLog, AuditFilter } from '../../auth/auth.model';
import { AuthService } from '../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-activity-log-list',
  templateUrl: './activity-log-list.component.html'
})
export class ActivityLogListComponent implements OnInit {
  logs: ActivityLog[] = [];
  filter: AuditFilter = this.emptyFilter();
  loading = false;
  exporting = false;
  errorMessage = '';
  page = 0;
  size = 25;
  totalElements = 0;
  totalPages = 0;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.getActivityLogs(this.filter, this.page, this.size).subscribe({
      next: page => {
        this.logs = page.content;
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
        this.page = page.number;
        this.size = page.size;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Activity logs could not be loaded.');
        debugApiError('ActivityLogListComponent.load', error);
      }
    });
  }

  reset(): void {
    this.filter = this.emptyFilter();
    this.page = 0;
    this.load();
  }

  applyFilter(): void {
    this.page = 0;
    this.load();
  }

  previousPage(): void {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.load();
    }
  }

  canExport(): boolean {
    return this.authService.hasPermission('ACTIVITY_LOG_EXPORT');
  }

  exportCsv(): void {
    this.exporting = true;
    this.errorMessage = '';
    this.authService.exportActivityLogs(this.filter).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'activity-logs.csv';
        link.click();
        window.URL.revokeObjectURL(url);
        this.exporting = false;
      },
      error: error => {
        this.exporting = false;
        this.errorMessage = extractApiErrorMessage(error, 'Activity logs could not be exported.');
        debugApiError('ActivityLogListComponent.exportCsv', error);
      }
    });
  }

  private emptyFilter(): AuditFilter {
    return { fromDate: '', toDate: '', username: '', action: '', module: '', search: '' };
  }
}
