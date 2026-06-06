import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { AuthRoutingModule } from './auth-routing.module';
import { LoginComponent } from './login/login.component';
import { UsersListComponent } from './users-list/users-list.component';
import { UserFormComponent } from './user-form/user-form.component';
import { RolePermissionsComponent } from './role-permissions/role-permissions.component';
import { ActivityLogListComponent } from './activity-log-list/activity-log-list.component';
import { AuditLogListComponent } from './audit-log-list/audit-log-list.component';
import { LoginHistoryListComponent } from './login-history-list/login-history-list.component';

@NgModule({
  declarations: [
    LoginComponent,
    UsersListComponent,
    UserFormComponent,
    RolePermissionsComponent,
    ActivityLogListComponent,
    AuditLogListComponent,
    LoginHistoryListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    AuthRoutingModule
  ]
})
export class AuthModule { }
