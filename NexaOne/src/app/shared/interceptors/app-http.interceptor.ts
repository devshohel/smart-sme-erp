import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { extractApiErrorMessage } from '../utils/api-error.util';
import { LoadingService } from '../services/loading.service';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class AppHttpInterceptor implements HttpInterceptor {
  private static readonly FORBIDDEN_NOTIFICATION_DEBOUNCE_MS = 3000;
  private lastForbiddenNotificationAt = 0;

  constructor(
    private loadingService: LoadingService,
    private notificationService: NotificationService
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const trackLoading = request.headers.get('X-Skip-Loading') !== 'true';
    const cleanRequest = request.clone({ headers: request.headers.delete('X-Skip-Loading') });

    if (trackLoading) {
      this.loadingService.show();
    }

    return next.handle(cleanRequest).pipe(
      catchError(error => this.handleError(error, cleanRequest)),
      finalize(() => {
        if (trackLoading) {
          this.loadingService.hide();
        }
      })
    );
  }

  private handleError(error: unknown, request: HttpRequest<unknown>): Observable<never> {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        // JwtInterceptor owns 401 recovery and session-expiry handling.
      } else if (error.status === 403) {
        const now = Date.now();
        if (now - this.lastForbiddenNotificationAt >= AppHttpInterceptor.FORBIDDEN_NOTIFICATION_DEBOUNCE_MS) {
          this.lastForbiddenNotificationAt = now;
          this.notificationService.warning('You do not have permission to perform this action.');
        }
      } else if (error.status === 404) {
        this.notificationService.error('The requested record could not be found.');
      } else if (error.status >= 500) {
        this.notificationService.error('Server error. Please try again or contact support.');
      } else if (error.status === 400 && !request.url.endsWith('/auth/refresh')) {
        this.notificationService.warning(extractApiErrorMessage(error, 'Please check the submitted information.'));
      }
    }

    return throwError(() => error);
  }
}
