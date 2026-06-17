import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    if (!this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/login']);
    }

    const permissions = route.data?.['permissions'] as string[] | undefined;
    const anyPermissions = route.data?.['anyPermissions'] as string[] | undefined;
    const required = anyPermissions?.length ? anyPermissions : permissions;

    if (!required?.length) {
      return true;
    }

    return this.authService.hasAnyPermission(required)
      ? true
      : this.router.createUrlTree(['/unauthorized']);
  }
}
