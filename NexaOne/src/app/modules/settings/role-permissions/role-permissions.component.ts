import { Component, OnInit } from '@angular/core';
import { Permission, Role } from '../../auth/auth.model';
import { AuthService } from '../../auth/auth.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-role-permissions',
  templateUrl: './role-permissions.component.html'
})
export class RolePermissionsComponent implements OnInit {
  roles: Role[] = [];
  permissions: Permission[] = [];
  selectedRoleId: number | null = null;
  selectedPermissionIds = new Set<number>();
  loading = false;
  saving = false;
  errorMessage = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loading = true;
    this.authService.getRoles().subscribe({
      next: roles => {
        this.roles = roles;
        this.selectedRoleId = roles[0]?.id || null;
        this.loadPermissions();
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Roles could not be loaded.');
        debugApiError('RolePermissionsComponent.loadRoles', error);
      }
    });
  }

  onRoleChange(): void {
    this.loadRolePermissions();
  }

  togglePermission(permission: Permission, checked: boolean): void {
    if (checked) {
      this.selectedPermissionIds.add(permission.id);
    } else {
      this.selectedPermissionIds.delete(permission.id);
    }
  }

  save(): void {
    if (!this.selectedRoleId) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.authService.updateRolePermissions(this.selectedRoleId, Array.from(this.selectedPermissionIds)).subscribe({
      next: permissions => {
        this.selectedPermissionIds = new Set(permissions.map(permission => permission.id));
        this.saving = false;
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Role permissions could not be saved.');
        debugApiError('RolePermissionsComponent.save', error);
      }
    });
  }

  groupedPermissions(): { module: string; permissions: Permission[] }[] {
    const groups = new Map<string, Permission[]>();
    this.permissions.forEach(permission => {
      const rows = groups.get(permission.module) || [];
      rows.push(permission);
      groups.set(permission.module, rows);
    });
    return Array.from(groups.entries()).map(([module, permissions]) => ({ module, permissions }));
  }

  isChecked(permission: Permission): boolean {
    return this.selectedPermissionIds.has(permission.id);
  }

  private loadPermissions(): void {
    this.authService.getPermissionsCatalog().subscribe({
      next: permissions => {
        this.permissions = permissions;
        this.loadRolePermissions();
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Permissions could not be loaded.');
        debugApiError('RolePermissionsComponent.loadPermissions', error);
      }
    });
  }

  private loadRolePermissions(): void {
    if (!this.selectedRoleId) {
      this.loading = false;
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.authService.getRolePermissions(this.selectedRoleId).subscribe({
      next: permissions => {
        this.selectedPermissionIds = new Set(permissions.map(permission => permission.id));
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Role permissions could not be loaded.');
        debugApiError('RolePermissionsComponent.loadRolePermissions', error);
      }
    });
  }
}
