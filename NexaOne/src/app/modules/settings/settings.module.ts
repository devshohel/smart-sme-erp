import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { ActivityLogListComponent } from './activity-log-list/activity-log-list.component';
import { AuditLogListComponent } from './audit-log-list/audit-log-list.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { CompanySettingsComponent } from './company-settings/company-settings.component';
import { InvoiceSettingsComponent } from './invoice-settings/invoice-settings.component';
import { LoginHistoryListComponent } from './login-history-list/login-history-list.component';
import { RolePermissionsComponent } from './role-permissions/role-permissions.component';
import { SettingsRoutingModule } from './settings-routing.module';
import { SystemSettingsComponent } from './system-settings/system-settings.component';
import { TaxSettingsComponent } from './tax-settings/tax-settings.component';
import { UserFormComponent } from './user-form/user-form.component';
import { UsersListComponent } from './users-list/users-list.component';

@NgModule({
  declarations: [
    ActivityLogListComponent,
    AuditLogListComponent,
    ChangePasswordComponent,
    CompanySettingsComponent,
    InvoiceSettingsComponent,
    LoginHistoryListComponent,
    RolePermissionsComponent,
    SystemSettingsComponent,
    TaxSettingsComponent,
    UserFormComponent,
    UsersListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    SettingsRoutingModule
  ]
})
export class SettingsModule { }
