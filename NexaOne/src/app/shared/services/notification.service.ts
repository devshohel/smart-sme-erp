import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type NotificationType = 'success' | 'warning' | 'error';

export interface AppNotification {
  id: number;
  type: NotificationType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 1;
  private readonly lastShownAt = new Map<string, number>();
  private readonly notificationsSubject = new BehaviorSubject<AppNotification[]>([]);

  readonly notifications$ = this.notificationsSubject.asObservable();

  success(message: string): void {
    this.notify('success', message);
  }

  warning(message: string): void {
    this.notify('warning', message);
  }

  error(message: string): void {
    this.notify('error', message);
  }

  dismiss(id: number): void {
    this.notificationsSubject.next(this.notificationsSubject.value.filter(item => item.id !== id));
  }

  private notify(type: NotificationType, message: string): void {
    const key = `${type}:${message}`;
    const now = Date.now();
    if (now - (this.lastShownAt.get(key) || 0) < 4000) return;
    this.lastShownAt.set(key, now);
    const notification: AppNotification = { id: this.nextId++, type, message };
    this.notificationsSubject.next([...this.notificationsSubject.value, notification]);
    window.setTimeout(() => this.dismiss(notification.id), type === 'error' ? 6000 : 3500);
  }
}
