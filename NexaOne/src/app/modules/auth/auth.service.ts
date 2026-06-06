import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Status } from '../../models/product.model';
import { ApiResponse, unwrapApiResponse } from '../../shared/utils/api-response.util';
import { LoginRequest, LoginResponse, Role, User } from './auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly usersUrl = `${environment.apiUrl}/users`;
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

  logout(): void {
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

  getRoles(): Observable<Role[]> {
    return this.http
      .get<Role[] | ApiResponse<Role[]>>(`${this.usersUrl}/roles`)
      .pipe(map(response => unwrapApiResponse(response)));
  }

  private storeSession(response: LoginResponse): void {
    localStorage.setItem(this.tokenKey, response.accessToken);
    localStorage.setItem(this.userKey, JSON.stringify({
      username: response.username,
      role: response.role,
      loginTimestamp: response.loginTimestamp
    }));
  }
}
