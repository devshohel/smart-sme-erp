import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { AuthService } from '../../modules/auth/auth.service';
import { extractApiErrorMessage } from '../utils/api-error.util';
import { LoadingService } from '../services/loading.service';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class AppHttpInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private loadingService: LoadingService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const trackLoading = request.headers.get('X-Skip-Loading') !== 'true';
    const cleanRequest = request.clone({ headers: request.headers.delete('X-Skip-Loading') });

    if (trackLoading) {
      this.loadingService.show();
    }

    return next.handle(cleanRequest).pipe(
      catchError(error => this.handleError(error)),
      finalize(() => {
        if (trackLoading) {
          this.loadingService.hide();
        }
      })
    );
  }

  private handleError(error: unknown): Observable<never> {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this.authService.clearSession();
        this.loadingService.reset();
        this.notificationService.warning('Your session has expired. Please sign in again.');
        this.router.navigate(['/login']);
      } else if (error.status === 403) {
        this.notificationService.warning('You do not have permission to perform this action.');
      } else if (error.status === 404) {
        this.notificationService.error('The requested record could not be found.');
      } else if (error.status >= 500) {
        this.notificationService.error('Server error. Please try again or contact support.');
      } else if (error.status === 400) {
        this.notificationService.warning(extractApiErrorMessage(error, 'Please check the submitted information.'));
      }
    }

    return throwError(() => error);
  }
}
