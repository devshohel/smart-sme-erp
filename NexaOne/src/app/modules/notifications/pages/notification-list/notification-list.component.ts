import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationCenterType, NotificationFilters, NotificationItem, NotificationSeverity } from '../../../../models/notification-center.model';
import { AuthService } from '../../../auth/auth.service';
import { NotificationCenterService } from '../../../../services/notification-center.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.css']
})
export class NotificationListComponent implements OnInit {
  notifications: NotificationItem[] = [];
  loading = false;
  actionLoadingId: number | null = null;
  filters: NotificationFilters = { read: '', type: '', severity: '', page: 0, size: 20 };
  totalElements = 0;
  totalPages = 0;
  readonly types: NotificationCenterType[] = ['INFO', 'SUCCESS', 'WARNING', 'ERROR'];
  readonly severities: NotificationSeverity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private notificationCenterService: NotificationCenterService,
    private toastService: NotificationService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.notificationCenterService.getNotifications(this.filters).subscribe({
      next: page => {
        this.notifications = page.content || [];
        this.totalElements = page.totalElements || 0;
        this.totalPages = page.totalPages || 0;
        this.filters.page = page.number || 0;
        this.filters.size = page.size || this.filters.size;
        this.loading = false;
      },
      error: () => {
        this.notifications = [];
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filters.page = 0;
    this.load();
  }

  resetFilters(): void {
    this.filters = { read: '', type: '', severity: '', page: 0, size: 20 };
    this.load();
  }

  markAsRead(item: NotificationItem, event?: MouseEvent): void {
    event?.stopPropagation();
    if (item.read) {
      return;
    }
    this.actionLoadingId = item.id;
    this.notificationCenterService.markAsRead(item.id).subscribe({
      next: updated => {
        Object.assign(item, updated);
        this.actionLoadingId = null;
      },
      error: () => this.actionLoadingId = null
    });
  }

  markAllAsRead(): void {
    this.loading = true;
    this.notificationCenterService.markAllAsRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map(item => ({ ...item, read: true, readAt: item.readAt || new Date().toISOString() }));
        this.loading = false;
        this.toastService.success('All notifications marked as read');
      },
      error: () => this.loading = false
    });
  }

  delete(item: NotificationItem, event: MouseEvent): void {
    event.stopPropagation();
    if (!this.canManage()) {
      return;
    }
    this.actionLoadingId = item.id;
    this.notificationCenterService.deleteNotification(item.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(notification => notification.id !== item.id);
        this.totalElements = Math.max(0, this.totalElements - 1);
        this.actionLoadingId = null;
        this.toastService.success('Notification deleted');
      },
      error: () => this.actionLoadingId = null
    });
  }

  open(item: NotificationItem): void {
    if (!item.read) {
      this.notificationCenterService.markAsRead(item.id).subscribe({ error: () => undefined });
      item.read = true;
    }
    if (item.actionUrl) {
      this.router.navigateByUrl(item.actionUrl);
      return;
    }
    this.router.navigate(['/notifications', item.id], { state: { notification: item } });
  }

  pagePrevious(): void {
    if ((this.filters.page || 0) > 0) {
      this.filters.page = (this.filters.page || 0) - 1;
      this.load();
    }
  }

  pageNext(): void {
    if ((this.filters.page || 0) + 1 < this.totalPages) {
      this.filters.page = (this.filters.page || 0) + 1;
      this.load();
    }
  }

  canManage(): boolean {
    return this.authService.hasPermission('NOTIFICATION_MANAGE');
  }
}
