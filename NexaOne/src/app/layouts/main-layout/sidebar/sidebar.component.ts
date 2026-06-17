import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { AuthService } from '../../../modules/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() isMobileView = false;
  @Input() isMobileOpen = false;
  @Input() isCollapsed = false;
  @Output() closeRequested = new EventEmitter<void>();

  isProductsOpen = true;
  isInventoryOpen = true;
  isSalesOpen = true;
  isCustomersOpen = true;
  isSuppliersOpen = true;
  isPurchasesOpen = true;
  isAccountingOpen = true;
  isReportsOpen = true;
  isSettingsOpen = true;

  constructor(private authService: AuthService) {
    this.checkScreenSize();
  }

  @HostListener('window:resize')
  checkScreenSize(): void {
    this.isMobileView = window.innerWidth < 992;
  }

  toggleProducts(): void {
    this.isProductsOpen = !this.isProductsOpen;
  }

  toggleInventory(): void {
    this.isInventoryOpen = !this.isInventoryOpen;
  }

  toggleSales(): void {
    this.isSalesOpen = !this.isSalesOpen;
  }

  toggleCustomers(): void {
    this.isCustomersOpen = !this.isCustomersOpen;
  }

  toggleSuppliers(): void {
    this.isSuppliersOpen = !this.isSuppliersOpen;
  }

  togglePurchases(): void {
    this.isPurchasesOpen = !this.isPurchasesOpen;
  }

  toggleAccounting(): void {
    this.isAccountingOpen = !this.isAccountingOpen;
  }

  toggleReports(): void {
    this.isReportsOpen = !this.isReportsOpen;
  }

  toggleSettings(): void {
    this.isSettingsOpen = !this.isSettingsOpen;
  }

  logout(): void {
    this.authService.logout();
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return this.authService.hasAnyPermission(permissions);
  }

  canViewSettingsMenu(): boolean {
    return this.hasAnyPermission(['SETTINGS_VIEW', 'USER_VIEW', 'ROLE_VIEW', 'ACTIVITY_VIEW', 'AUDIT_VIEW', 'LOGIN_HISTORY_VIEW']);
  }

  canViewActivityLogs(): boolean {
    return this.hasPermission('ACTIVITY_VIEW');
  }

  canViewAuditLogs(): boolean {
    return this.canShowAuditLogs();
  }

  canViewLoginHistory(): boolean {
    return this.canShowLoginHistory();
  }

  canShowAuditLogs(): boolean {
    return this.hasPermission('AUDIT_VIEW');
  }

  canShowLoginHistory(): boolean {
    return this.hasAnyPermission(['LOGIN_HISTORY_VIEW', 'AUDIT_VIEW']);
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN') || this.authService.isSuperAdmin();
  }

  closeSidebar(): void {
    if (this.isMobileView) {
      this.closeRequested.emit();
    }
  }
}
