import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { NotificationService } from '../../shared/services/notification.service';
import { AuthService } from './auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();
    const authRequest = token && this.shouldAttachAccessToken(request)
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;
    return next.handle(authRequest).pipe(
      catchError(error => {
        if (!(error instanceof HttpErrorResponse)
          || error.status !== 401
          || !this.canRefresh(request)
          || !this.authService.getRefreshToken()) {
          if (error instanceof HttpErrorResponse && error.status === 401 && this.canRefresh(request)) {
            this.endExpiredSession();
          }
          return throwError(() => error);
        }

        return this.authService.refreshSession().pipe(
          catchError(refreshError => {
            this.endExpiredSession();
            return throwError(() => refreshError);
          }),
          switchMap(() => {
            const refreshedToken = this.authService.getToken();
            if (!refreshedToken) {
              this.endExpiredSession();
              return throwError(() => new Error('Access token was not returned by refresh'));
            }
            return next.handle(request.clone({
              setHeaders: { Authorization: `Bearer ${refreshedToken}` }
            })).pipe(
              catchError(retryError => {
                if (retryError instanceof HttpErrorResponse && retryError.status === 401) {
                  this.endExpiredSession();
                }
                return throwError(() => retryError);
              })
            );
          })
        );
      })
    );
  }

  private shouldAttachAccessToken(request: HttpRequest<unknown>): boolean {
    return !request.url.endsWith('/auth/login') && !request.url.endsWith('/auth/refresh');
  }

  private canRefresh(request: HttpRequest<unknown>): boolean {
    return !request.url.endsWith('/auth/login')
      && !request.url.endsWith('/auth/refresh')
      && !request.url.endsWith('/auth/logout');
  }

  private endExpiredSession(): void {
    if (this.authService.expireSession()) {
      this.notificationService.warning('Your session has expired. Please sign in again.');
    }
  }
}
