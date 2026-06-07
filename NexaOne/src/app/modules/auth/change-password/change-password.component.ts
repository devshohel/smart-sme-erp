import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { ChangePasswordRequest } from '../auth.model';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  form: ChangePasswordRequest = {
    oldPassword: '',
    newPassword: '',
    confirmNewPassword: ''
  };
  loading = false;
  successMessage = '';
  errorMessage = '';
  showOldPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(private authService: AuthService) {}

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.newPassword !== this.form.confirmNewPassword) {
      this.errorMessage = 'New password and confirmation do not match.';
      return;
    }

    this.loading = true;
    this.authService.changePassword(this.form).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Password changed successfully. Signing you out.';
        setTimeout(() => this.authService.logout(), 800);
      },
      error: error => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Unable to change password.');
        debugApiError('ChangePasswordComponent.submit', error);
      }
    });
  }
}
