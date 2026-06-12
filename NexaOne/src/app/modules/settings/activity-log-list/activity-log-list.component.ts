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
  errorMessage = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.getActivityLogs(this.filter).subscribe({
      next: logs => {
        this.logs = logs;
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
    this.load();
  }

  private emptyFilter(): AuditFilter {
    return { fromDate: '', toDate: '', username: '', action: '', module: '' };
  }
}
