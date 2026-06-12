import { Component, OnInit } from '@angular/core';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';
import { InvoiceSettings } from '../../auth/auth.model';
import { SettingsService } from '../settings.service';

@Component({
  selector: 'app-invoice-settings',
  templateUrl: './invoice-settings.component.html'
})
export class InvoiceSettingsComponent implements OnInit {
  settings: InvoiceSettings = this.emptySettings();
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
    this.settingsService.getInvoice().subscribe({
      next: settings => {
        this.settings = settings;
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Invoice settings could not be loaded.');
        debugApiError('InvoiceSettingsComponent.load', error);
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
    this.settingsService.updateInvoice(this.settings).subscribe({
      next: settings => {
        this.settings = settings;
        this.saving = false;
        this.successMessage = 'Invoice settings saved.';
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Invoice settings could not be saved.');
        debugApiError('InvoiceSettingsComponent.save', error);
      }
    });
  }

  canEdit(): boolean {
    return this.authService.hasPermission('SETTINGS_EDIT');
  }

  private emptySettings(): InvoiceSettings {
    return {
      salesInvoicePrefix: 'INV',
      purchaseInvoicePrefix: 'PINV',
      salesOrderPrefix: 'SO',
      purchaseOrderPrefix: 'PO',
      nextInvoiceNumber: 1,
      invoiceFooterText: '',
      defaultPaymentTerms: ''
    };
  }
}
