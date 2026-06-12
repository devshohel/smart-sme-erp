import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActivityLogListComponent } from './activity-log-list/activity-log-list.component';
import { AuditLogListComponent } from './audit-log-list/audit-log-list.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { CompanySettingsComponent } from './company-settings/company-settings.component';
import { InvoiceSettingsComponent } from './invoice-settings/invoice-settings.component';
import { LoginHistoryListComponent } from './login-history-list/login-history-list.component';
import { RolePermissionsComponent } from './role-permissions/role-permissions.component';
import { SystemSettingsComponent } from './system-settings/system-settings.component';
import { TaxSettingsComponent } from './tax-settings/tax-settings.component';
import { UserFormComponent } from './user-form/user-form.component';
import { UsersListComponent } from './users-list/users-list.component';

const routes: Routes = [
  { path: 'company', component: CompanySettingsComponent },
  { path: 'invoice', component: InvoiceSettingsComponent },
  { path: 'tax', component: TaxSettingsComponent },
  { path: 'system', component: SystemSettingsComponent },
  { path: 'users', component: UsersListComponent },
  { path: 'users/create', component: UserFormComponent },
  { path: 'users/edit/:id', component: UserFormComponent },
  { path: 'roles-permissions', component: RolePermissionsComponent },
  { path: 'activity-logs', component: ActivityLogListComponent },
  { path: 'audit-logs', component: AuditLogListComponent },
  { path: 'login-history', component: LoginHistoryListComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: '', redirectTo: 'company', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule { }
