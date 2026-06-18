import { Component, OnInit } from '@angular/core';
import { Status } from '../../../models/product.model';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';
import { TaxSettings } from '../../auth/auth.model';
import { SettingsService } from '../settings.service';
import { AccountingService } from '../../accounting/accounting.service';
import { Account } from '../../accounting/accounting.model';

@Component({
  selector: 'app-tax-settings',
  templateUrl: './tax-settings.component.html'
})
export class TaxSettingsComponent implements OnInit {
  settings: TaxSettings = this.emptySettings();
  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];
  accounts: Account[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  constructor(private settingsService: SettingsService, private authService: AuthService, private accountingService: AccountingService) {}

  ngOnInit(): void {
    this.load();
    this.accountingService.getAccounts().subscribe({ next: accounts => this.accounts = accounts });
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.settingsService.getTax().subscribe({
      next: settings => {
        this.settings = settings;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Tax settings could not be loaded.');
        debugApiError('TaxSettingsComponent.load', error);
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
    this.settingsService.updateTax(this.settings).subscribe({
      next: settings => {
        this.settings = settings;
        this.saving = false;
        this.successMessage = 'Tax settings saved.';
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Tax settings could not be saved.');
        debugApiError('TaxSettingsComponent.save', error);
      }
    });
  }

  canEdit(): boolean {
    return this.authService.hasPermission('SETTINGS_EDIT');
  }

  private emptySettings(): TaxSettings {
    return { taxName: 'VAT', taxRate: 0, status: 'ACTIVE', defaultTaxEnabled: false, taxReceivableAccountId: null };
  }
}
