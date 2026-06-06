import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Status } from '../../../models/product.model';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { User } from '../auth.model';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html'
})
export class UsersListComponent implements OnInit {
  users: User[] = [];
  loading = false;
  errorMessage = '';
  filters = { keyword: '', status: '' as Status | '' };
  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.getUsers(this.filters.keyword, this.filters.status).subscribe({
      next: users => {
        this.users = users;
        this.loading = false;
      },
      error: error => {
        this.users = [];
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Users could not be loaded.');
        debugApiError('UsersListComponent.loadUsers', error);
      }
    });
  }

  resetFilters(): void {
    this.filters = { keyword: '', status: '' };
    this.loadUsers();
  }

  createUser(): void {
    this.router.navigate(['/settings/users/create']);
  }

  editUser(user: User): void {
    if (user.id) {
      this.router.navigate(['/settings/users/edit', user.id]);
    }
  }

  deactivateUser(user: User): void {
    if (!user.id || !confirm(`Deactivate user "${user.username}"?`)) {
      return;
    }
    this.authService.deactivateUser(user.id).subscribe({
      next: () => this.loadUsers(),
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Deactivate request failed.')
    });
  }

  deleteUser(user: User): void {
    if (!user.id || !confirm(`Delete user "${user.username}"?`)) {
      return;
    }
    this.authService.deleteUser(user.id).subscribe({
      next: () => this.loadUsers(),
      error: error => this.errorMessage = extractApiErrorMessage(error, 'Delete request failed.')
    });
  }

  statusClass(status?: Status): string {
    return status === 'ACTIVE' ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary';
  }
}
