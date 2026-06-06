import { Component, EventEmitter, Output } from '@angular/core';
import { AuthService } from '../../../modules/auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

  @Output()
  sidebarToggle = new EventEmitter<void>();

  constructor(private authService: AuthService) {}

  toggleSidebar(): void {
    this.sidebarToggle.emit();
  }

  logout(): void {
    this.authService.logout();
  }

  get username(): string {
    return this.authService.getCurrentUser()?.username || 'User';
  }
}
