import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationItem } from '../../../../models/notification-center.model';
import { NotificationCenterService } from '../../../../services/notification-center.service';

@Component({
  selector: 'app-notification-details',
  templateUrl: './notification-details.component.html',
  styleUrls: ['./notification-details.component.css']
})
export class NotificationDetailsComponent implements OnInit {
  notification?: NotificationItem;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private notificationCenterService: NotificationCenterService
  ) {}

  ngOnInit(): void {
    const stateNotification = history.state?.notification as NotificationItem | undefined;
    if (stateNotification) {
      this.notification = stateNotification;
      this.markReadIfNeeded(stateNotification);
      return;
    }

    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      return;
    }
    this.loading = true;
    this.notificationCenterService.getNotifications({ page: 0, size: 100 }).subscribe({
      next: page => {
        this.notification = (page.content || []).find(item => item.id === id);
        if (this.notification) {
          this.markReadIfNeeded(this.notification);
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openAction(): void {
    if (this.notification?.actionUrl) {
      this.router.navigateByUrl(this.notification.actionUrl);
    }
  }

  back(): void {
    this.router.navigate(['/notifications']);
  }

  private markReadIfNeeded(notification: NotificationItem): void {
    if (!notification.read) {
      this.notificationCenterService.markAsRead(notification.id).subscribe({
        next: updated => this.notification = updated,
        error: () => undefined
      });
    }
  }
}
