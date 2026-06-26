import { Component } from '@angular/core';
import { AppNotification, NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification-container',
  templateUrl: './notification-container.component.html',
  styleUrls: ['./notification-container.component.css']
})
export class NotificationContainerComponent {
  notifications$ = this.notificationService.notifications$;

  constructor(private notificationService: NotificationService) {}

  dismiss(notification: AppNotification): void {
    this.notificationService.dismiss(notification.id);
  }
}
