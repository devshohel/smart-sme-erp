export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

function isWrappedResponse<T>(response: unknown): response is ApiResponse<T> {
  if (!response || typeof response !== 'object') {
    return false;
  }

  const responseObject = response as Record<string, unknown>;
  return 'data' in responseObject && 'success' in responseObject;
}

export function unwrapApiResponse<T>(response: T | ApiResponse<T>): T {
  return isWrappedResponse<T>(response) ? response.data : response;
}
