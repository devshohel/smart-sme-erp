import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PermissionGuard } from '../auth/permission.guard';
import { NotificationDetailsComponent } from './pages/notification-details/notification-details.component';
import { NotificationListComponent } from './pages/notification-list/notification-list.component';

const routes: Routes = [
  {
    path: '',
    component: NotificationListComponent,
    canActivate: [PermissionGuard],
    data: { permissions: ['NOTIFICATION_VIEW'], breadcrumb: 'Notifications' }
  },
  {
    path: ':id',
    component: NotificationDetailsComponent,
    canActivate: [PermissionGuard],
    data: { permissions: ['NOTIFICATION_VIEW'], breadcrumb: 'Notification Details' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule {}
