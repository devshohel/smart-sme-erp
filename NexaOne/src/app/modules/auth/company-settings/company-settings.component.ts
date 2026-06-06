import { Component, OnInit } from '@angular/core';
import { Status } from '../../../models/product.model';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../auth.service';
import { CompanySettings } from '../auth.model';
import { SettingsService } from '../settings.service';

@Component({
  selector: 'app-company-settings',
  templateUrl: './company-settings.component.html'
})
export class CompanySettingsComponent implements OnInit {
  settings: CompanySettings = this.emptySettings();
  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];
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
    this.settingsService.getCompany().subscribe({
      next: settings => {
        this.settings = settings;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Company settings could not be loaded.');
        debugApiError('CompanySettingsComponent.load', error);
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
    this.settingsService.updateCompany(this.settings).subscribe({
      next: settings => {
        this.settings = settings;
        this.saving = false;
        this.successMessage = 'Company settings saved.';
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Company settings could not be saved.');
        debugApiError('CompanySettingsComponent.save', error);
      }
    });
  }

  canEdit(): boolean {
    return this.authService.hasPermission('SETTINGS_EDIT');
  }

  private emptySettings(): CompanySettings {
    return {
      companyName: '',
      businessType: '',
      email: '',
      phone: '',
      address: '',
      city: '',
      country: '',
      logoUrl: '',
      taxNumber: '',
      currency: 'BDT',
      timezone: 'Asia/Dhaka',
      status: 'ACTIVE'
    };
  }
}
