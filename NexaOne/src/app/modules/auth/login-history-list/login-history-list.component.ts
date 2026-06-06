import { Component, OnInit } from '@angular/core';
import { AuditFilter, LoginHistory } from '../auth.model';
import { AuthService } from '../auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-login-history-list',
  templateUrl: './login-history-list.component.html'
})
export class LoginHistoryListComponent implements OnInit {
  rows: LoginHistory[] = [];
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
    this.authService.getLoginHistory(this.filter).subscribe({
      next: rows => {
        this.rows = rows;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Login history could not be loaded.');
        debugApiError('LoginHistoryListComponent.load', error);
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
