import { Component, OnInit } from '@angular/core';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../auth.service';
import { SystemSettings } from '../auth.model';
import { SettingsService } from '../settings.service';

@Component({
  selector: 'app-system-settings',
  templateUrl: './system-settings.component.html'
})
export class SystemSettingsComponent implements OnInit {
  settings: SystemSettings = this.emptySettings();
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  constructor(private settingsService: SettingsService, private authService: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.settingsService.getSystem().subscribe({
      next: settings => {
        this.settings = settings;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'System settings could not be loaded.');
        debugApiError('SystemSettingsComponent.load', error);
      }
    });
  }

  save(): void {
    if (!this.canEdit()) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.settingsService.updateSystem(this.settings).subscribe({
      next: settings => {
        this.settings = settings;
        this.saving = false;
        this.successMessage = 'System settings saved.';
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'System settings could not be saved.');
        debugApiError('SystemSettingsComponent.save', error);
      }
    });
  }

  canEdit(): boolean {
    return this.authService.hasPermission('SETTINGS_EDIT');
  }

  private emptySettings(): SystemSettings {
    return {
      defaultCurrency: 'BDT',
      dateFormat: 'yyyy-MM-dd',
      numberFormat: '#,##0.00',
      lowStockAlertEnabled: true,
      dashboardRefreshEnabled: true
    };
  }
}
