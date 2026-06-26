import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { NotificationFilters, NotificationItem, NotificationPage } from '../models/notification-center.model';
import { ApiResponse, unwrapApiResponse } from '../shared/utils/api-response.util';

@Injectable({ providedIn: 'root' })
export class NotificationCenterService {
  private readonly apiUrl = `${environment.apiUrl}/notifications`;
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);

  unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  getNotifications(filters: NotificationFilters = {}): Observable<NotificationPage> {
    return this.http
      .get<NotificationPage | ApiResponse<NotificationPage>>(this.apiUrl, { params: this.buildParams(filters) })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getPreview(): Observable<NotificationItem[]> {
    return this.getNotifications({ page: 0, size: 5 }).pipe(map(page => page.content || []));
  }

  getUnreadCount(): Observable<number> {
    return this.http
      .get<{ count: number } | ApiResponse<{ count: number }>>(`${this.apiUrl}/unread-count`)
      .pipe(
        map(response => unwrapApiResponse(response).count || 0),
        tap(count => this.unreadCountSubject.next(count))
      );
  }

  markAsRead(id: number): Observable<NotificationItem> {
    return this.http
      .put<NotificationItem | ApiResponse<NotificationItem>>(`${this.apiUrl}/${id}/read`, {})
      .pipe(
        map(response => unwrapApiResponse(response)),
        tap(() => this.refreshUnreadCount())
      );
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/read-all`, {}).pipe(
      tap(() => this.unreadCountSubject.next(0))
    );
  }

  deleteNotification(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.refreshUnreadCount())
    );
  }

  refreshUnreadCount(): void {
    this.getUnreadCount().subscribe({ error: () => this.unreadCountSubject.next(0) });
  }

  private buildParams(filters: NotificationFilters): HttpParams {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 20));

    if (filters.read !== undefined && filters.read !== '') {
      params = params.set('read', String(filters.read));
    }
    if (filters.type) {
      params = params.set('type', filters.type);
    }
    if (filters.severity) {
      params = params.set('severity', filters.severity);
    }
    if (filters.fromDate) {
      params = params.set('fromDate', `${filters.fromDate}T00:00:00`);
    }
    if (filters.toDate) {
      params = params.set('toDate', `${filters.toDate}T23:59:59`);
    }

    return params;
  }
}
