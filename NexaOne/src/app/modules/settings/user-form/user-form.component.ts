import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Status } from '../../../models/product.model';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { Role, User } from '../../auth/auth.model';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html'
})
export class UserFormComponent implements OnInit {
  user: User = this.emptyUser();
  roles: Role[] = [];
  isEdit = false;
  loading = false;
  saving = false;
  errorMessage = '';
  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE'];

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRoles();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.isEdit = true;
      this.loadUser(id);
    }
  }

  save(): void {
    this.saving = true;
    this.errorMessage = '';
    const payload: User = {
      ...this.user,
      password: this.user.password?.trim() ? this.user.password : null
    };

    this.authService.saveUser(payload).subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/settings/users']);
      },
      error: error => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'User could not be saved.');
        debugApiError('UserFormComponent.save', error);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/settings/users']);
  }

  private loadRoles(): void {
    this.authService.getRoles().subscribe({
      next: roles => this.roles = roles,
      error: error => {
        this.errorMessage = extractApiErrorMessage(error, 'Roles could not be loaded.');
        debugApiError('UserFormComponent.loadRoles', error);
      }
    });
  }

  private loadUser(id: number): void {
    this.loading = true;
    this.authService.getUser(id).subscribe({
      next: user => {
        this.user = { ...user, password: null };
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'User could not be loaded.');
        debugApiError('UserFormComponent.loadUser', error);
      }
    });
  }

  private emptyUser(): User {
    return {
      name: '',
      username: '',
      email: '',
      phone: '',
      password: '',
      roleId: null,
      status: 'ACTIVE'
    };
  }
}
