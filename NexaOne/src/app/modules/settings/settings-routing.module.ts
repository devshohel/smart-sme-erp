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
  { path: 'company', component: CompanySettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'], breadcrumb: 'Company Settings' } },
  { path: 'invoice', component: InvoiceSettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'], breadcrumb: 'Invoice Settings' } },
  { path: 'tax', component: TaxSettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'], breadcrumb: 'Tax Settings' } },
  { path: 'system', component: SystemSettingsComponent, canActivate: [PermissionGuard], data: { permissions: ['SETTINGS_VIEW'], breadcrumb: 'System Settings' } },
  { path: 'users', component: UsersListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['USER_VIEW', 'USER_CREATE', 'USER_EDIT'], breadcrumb: 'Users' } },
  { path: 'users/create', component: UserFormComponent, canActivate: [PermissionGuard], data: { permissions: ['USER_CREATE'], breadcrumb: 'Create User' } },
  { path: 'users/edit/:id', component: UserFormComponent, canActivate: [PermissionGuard], data: { permissions: ['USER_EDIT'], breadcrumb: 'Edit User' } },
  { path: 'roles-permissions', component: RolePermissionsComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['ROLE_VIEW', 'ROLE_EDIT'], breadcrumb: 'Roles & Permissions' } },
  { path: 'activity-logs', component: ActivityLogListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['AUDIT_VIEW', 'ACTIVITY_LOG_VIEW', 'ACTIVITY_VIEW'], breadcrumb: 'Activity Logs' } },
  { path: 'audit-logs', component: AuditLogListComponent, canActivate: [PermissionGuard], data: { permissions: ['AUDIT_VIEW'], breadcrumb: 'Audit Logs' } },
  { path: 'login-history', component: LoginHistoryListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['LOGIN_HISTORY_VIEW', 'AUDIT_VIEW'], breadcrumb: 'Login History' } },
  { path: 'change-password', component: ChangePasswordComponent, data: { breadcrumb: 'Change Password' } },
  { path: '', redirectTo: 'company', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule { }
