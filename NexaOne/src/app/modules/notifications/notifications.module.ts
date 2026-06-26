import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { NotificationDetailsComponent } from './pages/notification-details/notification-details.component';
import { NotificationListComponent } from './pages/notification-list/notification-list.component';
import { NotificationsRoutingModule } from './notifications-routing.module';

@NgModule({
  declarations: [
    NotificationListComponent,
    NotificationDetailsComponent
  ],
  imports: [
    SharedModule,
    NotificationsRoutingModule
  ]
})
export class NotificationsModule {}
