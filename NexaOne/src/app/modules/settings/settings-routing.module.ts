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
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  { path: 'company', component: CompanySettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'] } },
  { path: 'invoice', component: InvoiceSettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'] } },
  { path: 'tax', component: TaxSettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'] } },
  { path: 'system', component: SystemSettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'] } },
  { path: 'users', component: UsersListComponent, canActivate: [PermissionGuard], data: { permissions: ['USER_VIEW'] } },
  { path: 'users/create', component: UserFormComponent, canActivate: [PermissionGuard], data: { permissions: ['USER_CREATE'] } },
  { path: 'users/edit/:id', component: UserFormComponent, canActivate: [PermissionGuard], data: { permissions: ['USER_EDIT'] } },
  { path: 'roles-permissions', component: RolePermissionsComponent, canActivate: [PermissionGuard], data: { permissions: ['ROLE_VIEW'] } },
  { path: 'activity-logs', component: ActivityLogListComponent, canActivate: [PermissionGuard], data: { permissions: ['ACTIVITY_VIEW'] } },
  { path: 'audit-logs', component: AuditLogListComponent, canActivate: [PermissionGuard], data: { permissions: ['AUDIT_VIEW'] } },
  { path: 'login-history', component: LoginHistoryListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['LOGIN_HISTORY_VIEW', 'AUDIT_VIEW'] } },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: '', redirectTo: 'company', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule { }
