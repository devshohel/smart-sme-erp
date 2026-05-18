import { HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface ErrorBody {
  message?: string;
  error?: string;
  errors?: Record<string, string>;
}

export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const body = error.error as ErrorBody | string | null;
  if (typeof body === 'string' && body.trim()) {
    return body;
  }

  if (body && typeof body === 'object') {
    if (body.errors) {
      const validationMessage = Object.values(body.errors).find(message => !!message?.trim());
      if (validationMessage) {
        return validationMessage;
      }
    }

    if (body.message?.trim()) {
      return body.message;
    }

    if (body.error?.trim()) {
      return body.error;
    }
  }

  return fallback;
}

export function debugApiError(context: string, error: unknown): void {
  if (!environment.production) {
    console.error(`[${context}]`, error);
  }
}
