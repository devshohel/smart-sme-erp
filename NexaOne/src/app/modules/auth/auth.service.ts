import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Status } from '../../models/product.model';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';
import { ActivityLog, AuditFilter, AuditLog, ChangePasswordRequest, LoginHistory, LoginRequest, LoginResponse, PageResponse, Permission, Role, User } from './auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly usersUrl = `${environment.apiUrl}/users`;
  private readonly auditUrl = `${environment.apiUrl}/audit`;
  private readonly tokenKey = 'sme_access_token';
  private readonly userKey = 'sme_auth_user';

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse | ApiResponse<LoginResponse>>(`${this.authUrl}/login`, request)
      .pipe(
        map(response => unwrapApiResponse(response)),
        tap(response => this.storeSession(response))
      );
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/change-password`, request);
  }

  logout(): void {
    if (this.getToken()) {
      this.http.post<void>(`${this.authUrl}/logout`, {}).subscribe({ error: () => undefined });
    }
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getCurrentUser(): Partial<LoginResponse> | null {
    const value = localStorage.getItem(this.userKey);
    return value ? JSON.parse(value) : null;
  }

  getPermissions(): string[] {
    return this.getCurrentUser()?.permissions || [];
  }

  hasPermission(permission: string): boolean {
    return !!permission && (this.isSuperAdmin() || this.getPermissions().includes(permission));
  }

  hasAnyPermission(permissions: string[]): boolean {
    return !!permissions?.length && (this.isSuperAdmin() || permissions.some(permission => this.hasPermission(permission)));
  }

  hasAllPermissions(permissions: string[]): boolean {
    return !!permissions?.length && (this.isSuperAdmin() || permissions.every(permission => this.hasPermission(permission)));
  }

  hasRole(role: string): boolean {
    const currentRole = this.normalizeRole(this.getCurrentUser()?.role);
    return !!role && currentRole === this.normalizeRole(role);
  }

  isSuperAdmin(): boolean {
    return this.hasRole('SUPER_ADMIN');
  }

  can(permission: string): boolean {
    return this.hasPermission(permission);
  }

  getUsers(keyword?: string, status?: Status | ''): Observable<User[]> {
    let params = new HttpParams();
    if (keyword?.trim()) {
      params = params.set('keyword', keyword.trim());
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http
      .get<User[] | ApiResponse<User[]>>(this.usersUrl, { params })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getDeletedUsers(): Observable<User[]> {
    return this.http
      .get<User[] | ApiResponse<User[]>>(`${this.usersUrl}/deleted`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getUser(id: number): Observable<User> {
    return this.http
      .get<User | ApiResponse<User>>(`${this.usersUrl}/${id}`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  saveUser(user: User): Observable<User> {
    const request$ = user.id
      ? this.http.put<User | ApiResponse<User>>(`${this.usersUrl}/${user.id}`, user)
      : this.http.post<User | ApiResponse<User>>(this.usersUrl, user);
    return request$.pipe(map(response => unwrapApiResponse(response)));
  }

  deactivateUser(id: number): Observable<User> {
    return this.http
      .put<User | ApiResponse<User>>(`${this.usersUrl}/${id}/deactivate`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.usersUrl}/${id}`);
  }

  restoreUser(id: number): Observable<User> {
    return this.http
      .put<User | ApiResponse<User>>(`${this.usersUrl}/${id}/restore`, {})
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getRoles(): Observable<Role[]> {
    return this.http
      .get<Role[] | ApiResponse<Role[]>>(`${this.usersUrl}/roles`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getPermissionsCatalog(): Observable<Permission[]> {
    return this.http
      .get<Permission[] | ApiResponse<Permission[]>>(`${environment.apiUrl}/permissions`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getRolePermissions(roleId: number): Observable<Permission[]> {
    return this.http
      .get<Permission[] | ApiResponse<Permission[]>>(`${environment.apiUrl}/roles/${roleId}/permissions`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  updateRolePermissions(roleId: number, permissionIds: number[]): Observable<Permission[]> {
    return this.http
      .put<Permission[] | ApiResponse<Permission[]>>(`${environment.apiUrl}/roles/${roleId}/permissions`, { permissionIds })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getActivityLogs(filter: AuditFilter, page = 0, size = 25): Observable<PageResponse<ActivityLog>> {
    return this.http
      .get<PageResponse<ActivityLog> | ActivityLog[] | ApiResponse<PageResponse<ActivityLog> | ActivityLog[]>>(`${this.auditUrl}/activity-logs`, {
        params: this.buildAuditParams(filter).set('page', String(page)).set('size', String(size))
      })
      .pipe(map(response => this.toPage(unwrapApiResponse(response), page, size)));
  }

  exportActivityLogs(filter: AuditFilter): Observable<Blob> {
    return this.http.get(`${this.auditUrl}/activity-logs/export`, {
      params: this.buildAuditParams(filter),
      responseType: 'blob'
    });
  }

  getAuditLogs(filter: AuditFilter): Observable<AuditLog[]> {
    return this.http
      .get<AuditLog[] | ApiResponse<AuditLog[]>>(`${this.auditUrl}/audit-logs`, { params: this.buildAuditParams(filter) })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  getLoginHistory(filter: AuditFilter): Observable<LoginHistory[]> {
    return this.http
      .get<LoginHistory[] | ApiResponse<LoginHistory[]>>(`${this.auditUrl}/login-history`, { params: this.buildAuditParams(filter) })
      .pipe(map(response => unwrapApiResponse(response)));
  }

  private storeSession(response: LoginResponse): void {
    localStorage.setItem(this.tokenKey, response.accessToken);
    localStorage.setItem(this.userKey, JSON.stringify({
      name: response.name,
      username: response.username,
      role: response.role,
      permissions: response.permissions || [],
      loginTimestamp: response.loginTimestamp
    }));
  }

  private buildAuditParams(filter: AuditFilter): HttpParams {
    let params = new HttpParams();
    if (filter.fromDate) {
      params = params.set('fromDate', `${filter.fromDate}T00:00:00`);
    }
    if (filter.toDate) {
      params = params.set('toDate', `${filter.toDate}T23:59:59`);
    }
    if (filter.username?.trim()) {
      params = params.set('username', filter.username.trim());
    }
    if (filter.action?.trim()) {
      params = params.set('action', filter.action.trim());
    }
    if (filter.module?.trim()) {
      params = params.set('module', filter.module.trim());
    }
    if (filter.search?.trim()) {
      params = params.set('search', filter.search.trim());
    }
    return params;
  }

  private toPage<T>(response: PageResponse<T> | T[], page: number, size: number): PageResponse<T> {
    if (Array.isArray(response)) {
      return {
        content: response,
        totalElements: response.length,
        totalPages: 1,
        number: page,
        size
      };
    }
    return response;
  }

  private normalizeRole(role?: string): string {
    return (role || '').replace(/^ROLE_/, '').toUpperCase();
  }
}
