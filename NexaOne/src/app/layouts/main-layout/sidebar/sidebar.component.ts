import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { AuthService } from '../../../modules/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  private readonly menuStateKey = 'nexa_sidebar_menu_state';

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

  ngOnInit(): void {
    this.restoreMenuState();
  }

  @HostListener('window:resize')
  checkScreenSize(): void {
    this.isMobileView = window.innerWidth < 992;
  }

  toggleProducts(): void {
    this.isProductsOpen = !this.isProductsOpen;
    this.saveMenuState();
  }

  toggleInventory(): void {
    this.isInventoryOpen = !this.isInventoryOpen;
    this.saveMenuState();
  }

  toggleSales(): void {
    this.isSalesOpen = !this.isSalesOpen;
    this.saveMenuState();
  }

  toggleCustomers(): void {
    this.isCustomersOpen = !this.isCustomersOpen;
    this.saveMenuState();
  }

  toggleSuppliers(): void {
    this.isSuppliersOpen = !this.isSuppliersOpen;
    this.saveMenuState();
  }

  togglePurchases(): void {
    this.isPurchasesOpen = !this.isPurchasesOpen;
    this.saveMenuState();
  }

  toggleAccounting(): void {
    this.isAccountingOpen = !this.isAccountingOpen;
    this.saveMenuState();
  }

  toggleReports(): void {
    this.isReportsOpen = !this.isReportsOpen;
    this.saveMenuState();
  }

  toggleSettings(): void {
    this.isSettingsOpen = !this.isSettingsOpen;
    this.saveMenuState();
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
    return this.hasAnyPermission(['SETTINGS_VIEW', 'SETTINGS_EDIT', 'USER_VIEW', 'USER_CREATE', 'USER_EDIT', 'ROLE_VIEW', 'ACTIVITY_LOG_VIEW', 'ACTIVITY_VIEW', 'AUDIT_VIEW', 'LOGIN_HISTORY_VIEW']);
  }

  canViewActivityLogs(): boolean {
    return this.hasAnyPermission(['AUDIT_VIEW', 'ACTIVITY_LOG_VIEW', 'ACTIVITY_VIEW']);
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

  private restoreMenuState(): void {
    const state = localStorage.getItem(this.menuStateKey);
    if (!state) {
      return;
    }

    try {
      const parsed = JSON.parse(state);
      this.isProductsOpen = parsed.products ?? this.isProductsOpen;
      this.isInventoryOpen = parsed.inventory ?? this.isInventoryOpen;
      this.isSalesOpen = parsed.sales ?? this.isSalesOpen;
      this.isCustomersOpen = parsed.customers ?? this.isCustomersOpen;
      this.isSuppliersOpen = parsed.suppliers ?? this.isSuppliersOpen;
      this.isPurchasesOpen = parsed.purchases ?? this.isPurchasesOpen;
      this.isAccountingOpen = parsed.accounting ?? this.isAccountingOpen;
      this.isReportsOpen = parsed.reports ?? this.isReportsOpen;
      this.isSettingsOpen = parsed.settings ?? this.isSettingsOpen;
    } catch {
      localStorage.removeItem(this.menuStateKey);
    }
  }

  private saveMenuState(): void {
    localStorage.setItem(this.menuStateKey, JSON.stringify({
      products: this.isProductsOpen,
      inventory: this.isInventoryOpen,
      sales: this.isSalesOpen,
      customers: this.isCustomersOpen,
      suppliers: this.isSuppliersOpen,
      purchases: this.isPurchasesOpen,
      accounting: this.isAccountingOpen,
      reports: this.isReportsOpen,
      settings: this.isSettingsOpen
    }));
  }
}
