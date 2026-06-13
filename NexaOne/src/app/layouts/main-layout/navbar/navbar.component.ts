import { Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../modules/auth/auth.service';
import { DashboardService } from '../../../modules/dashboard/dashboard.service';
import { DueAlert, LowStockAlert, RecentTransaction } from '../../../modules/dashboard/dashboard.model';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  @ViewChild('searchInput')
  searchInput?: ElementRef<HTMLInputElement>;
  @Output()
  sidebarToggle = new EventEmitter<void>();
  @Input() isSidebarCollapsed = false;

  lowStockAlerts: LowStockAlert[] = [];
  dueAlerts: DueAlert[] = [];
  recentTransactions: RecentTransaction[] = [];
  isNotificationOpen = false;
  isSearchOpen = false;
  searchMessage = '';
  searchTerm = '';
  filteredSuggestions: { label: string; route: string; keywords: string[] }[] = [];
  private searchMessageTimer: ReturnType<typeof setTimeout> | null = null;

  private readonly searchItems: { label: string; route: string; keywords: string[] }[] = [
    { label: 'Dashboard', route: '/dashboard', keywords: ['dashboard'] },
    { label: 'All Products', route: '/products/products', keywords: ['product', 'products', 'all products'] },
    { label: 'Add Product', route: '/products/add-product', keywords: ['add product'] },
    { label: 'Categories', route: '/products/categories', keywords: ['category', 'categories'] },
    { label: 'Brands', route: '/products/brands', keywords: ['brand', 'brands'] },
    { label: 'Unit of Measures', route: '/products/uom', keywords: ['uom', 'unit', 'unit of measures'] },
    { label: 'Stock Levels', route: '/inventory/stocks', keywords: ['stock', 'current stock', 'stock levels'] },
    { label: 'Stock Movements', route: '/inventory/movements', keywords: ['stock movement', 'movement'] },
    { label: 'Stock Adjustments', route: '/inventory/adjustments', keywords: ['stock adjustment', 'adjustment'] },
    { label: 'Warehouses', route: '/inventory/warehouses', keywords: ['warehouse', 'warehouses'] },
    { label: 'Customer List', route: '/customers/list', keywords: ['customer', 'customers', 'customer list'] },
    { label: 'Add Customer', route: '/customers/create', keywords: ['customer form', 'add customer'] },
    { label: 'Supplier List', route: '/suppliers/list', keywords: ['supplier', 'suppliers', 'supplier list'] },
    { label: 'Add Supplier', route: '/suppliers/create', keywords: ['supplier form', 'add supplier'] },
    { label: 'Purchase Orders', route: '/purchases/orders', keywords: ['purchase', 'purchases', 'purchase order'] },
    { label: 'Purchase Invoices', route: '/purchases/invoices', keywords: ['purchase invoice', 'purchase receive'] },
    { label: 'Purchase Returns', route: '/purchases/returns', keywords: ['purchase return'] },
    { label: 'Sales Orders', route: '/sales/orders', keywords: ['sales', 'sale', 'sales order', 'sale order'] },
    { label: 'Sales Invoices', route: '/sales/invoices', keywords: ['invoice', 'sales invoice'] },
    { label: 'Sales Returns', route: '/sales/returns', keywords: ['sales return'] },
    { label: 'Sales Report', route: '/reports/sales', keywords: ['report', 'reports', 'sales report'] },
    { label: 'Purchase Report', route: '/reports/purchases', keywords: ['purchase report'] },
    { label: 'Stock Report', route: '/reports/stock', keywords: ['stock report'] },
    { label: 'Customer Due Report', route: '/reports/customer-dues', keywords: ['customer due report'] },
    { label: 'Supplier Due Report', route: '/reports/supplier-dues', keywords: ['supplier due report'] },
    { label: 'Profit & Loss Summary', route: '/reports/profit-loss', keywords: ['profit loss', 'profit and loss', 'p&l'] },
    { label: 'Expenses', route: '/accounting/expenses', keywords: ['accounting', 'expense', 'expenses'] },
    { label: 'Expense Categories', route: '/accounting/expense-categories', keywords: ['expense category'] },
    { label: 'Cash Book', route: '/accounting/cash-book', keywords: ['cash book'] },
    { label: 'Bank Book', route: '/accounting/bank-book', keywords: ['bank book'] },
    { label: 'Journal Entries', route: '/accounting/journal-entries', keywords: ['journal', 'journal entry'] },
    { label: 'Chart of Accounts', route: '/accounting/accounts', keywords: ['chart of accounts', 'accounts'] },
    { label: 'Customer Ledger', route: '/accounting/customer-ledger', keywords: ['customer ledger'] },
    { label: 'Supplier Ledger', route: '/accounting/supplier-ledger', keywords: ['supplier ledger'] },
    { label: 'General Ledger', route: '/accounting/general-ledger', keywords: ['general ledger'] },
    { label: 'Trial Balance', route: '/accounting/trial-balance', keywords: ['trial balance'] },
    { label: 'Balance Sheet', route: '/accounting/balance-sheet', keywords: ['balance sheet'] },
    { label: 'Company Settings', route: '/settings/company', keywords: ['settings', 'company settings'] },
    { label: 'Invoice Settings', route: '/settings/invoice', keywords: ['invoice settings'] },
    { label: 'Tax Settings', route: '/settings/tax', keywords: ['tax settings'] },
    { label: 'System Settings', route: '/settings/system', keywords: ['system settings'] },
    { label: 'Users', route: '/settings/users', keywords: ['users'] },
    { label: 'Roles & Permissions', route: '/settings/roles-permissions', keywords: ['roles', 'permissions', 'role permissions'] },
    { label: 'Change Password', route: '/settings/change-password', keywords: ['change password'] },
    { label: 'Activity Logs', route: '/settings/activity-logs', keywords: ['activity logs'] },
    { label: 'Audit Logs', route: '/settings/audit-logs', keywords: ['audit logs'] },
    { label: 'Login History', route: '/settings/login-history', keywords: ['login history'] }
  ];

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  @HostListener('document:click')
  closeNotificationDropdown(): void {
    this.isNotificationOpen = false;
    this.isSearchOpen = false;
  }

  toggleSidebar(): void {
    this.sidebarToggle.emit();
  }

  runSearch(keyword: string): void {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) {
      return;
    }

    const match = this.findSearchMatch(normalized);

    if (match) {
      this.navigateSearch(match.route);
      return;
    }

    this.showSearchMessage('No matching section found.');
  }

  onSearchInput(keyword: string): void {
    this.searchTerm = keyword;
    const normalized = keyword.trim().toLowerCase();
    if (normalized.length < 2) {
      this.filteredSuggestions = [];
      this.isSearchOpen = false;
      return;
    }
    this.filteredSuggestions = normalized
      ? this.searchItems.filter(item => this.matchesSearchItem(item, normalized)).slice(0, 6)
      : [];
    this.isSearchOpen = this.filteredSuggestions.length > 0;
  }

  keepSearchOpen(event: MouseEvent): void {
    event.stopPropagation();
  }

  navigateSearch(route: string): void {
    this.router.navigate([route]);
    this.searchMessage = '';
    this.searchTerm = '';
    this.filteredSuggestions = [];
    this.isSearchOpen = false;
    if (this.searchInput) {
      this.searchInput.nativeElement.value = '';
    }
  }

  toggleNotifications(event: MouseEvent): void {
    event.stopPropagation();
    this.isNotificationOpen = !this.isNotificationOpen;
  }

  keepNotificationsOpen(event: MouseEvent): void {
    event.stopPropagation();
  }

  navigateFromNotification(route: string): void {
    this.isNotificationOpen = false;
    this.router.navigate([route]);
  }

  recentTransactionRoute(item: RecentTransaction): string {
    const type = (item.type || '').toLowerCase();
    if (type.includes('sales invoice')) {
      return '/sales/invoices';
    }
    if (type.includes('purchase')) {
      return '/purchases/invoices';
    }
    if (type.includes('expense')) {
      return '/accounting/expenses';
    }
    if (type.includes('customer due')) {
      return '/reports/customer-dues';
    }
    if (type.includes('supplier due')) {
      return '/reports/supplier-dues';
    }
    if (type.includes('low stock')) {
      return '/inventory/stocks';
    }
    return '/dashboard';
  }

  dueAlertRoute(alert: DueAlert): string {
    return (alert.type || '').toLowerCase().includes('supplier')
      ? '/reports/supplier-dues'
      : '/reports/customer-dues';
  }

  logout(): void {
    this.authService.logout();
  }

  get displayName(): string {
    const currentUser = this.authService.getCurrentUser() as { name?: string; fullName?: string; username?: string } | null;
    return currentUser?.name?.trim() || currentUser?.fullName?.trim() || currentUser?.username || 'User';
  }

  get role(): string {
    const role = (this.authService.getCurrentUser()?.role || 'User').replace('ROLE_', '').replace(/_/g, ' ').toLowerCase();
    return role.replace(/\b\w/g, value => value.toUpperCase());
  }

  get notificationCount(): number {
    return this.lowStockAlerts.length + this.dueAlerts.length;
  }

  get hasNotifications(): boolean {
    return this.notificationCount > 0 || this.recentTransactions.length > 0;
  }

  private loadNotifications(): void {
    this.dashboardService.getSummary().subscribe({
      next: summary => {
        this.lowStockAlerts = summary.lowStockAlerts || [];
        this.dueAlerts = summary.dueAlerts || [];
        this.recentTransactions = (summary.recentTransactions || []).slice(0, 4);
      },
      error: () => {
        this.lowStockAlerts = [];
        this.dueAlerts = [];
        this.recentTransactions = [];
      }
    });
  }

  private showSearchMessage(message: string): void {
    this.searchMessage = message;
    if (this.searchMessageTimer) {
      clearTimeout(this.searchMessageTimer);
    }
    this.searchMessageTimer = setTimeout(() => this.searchMessage = '', 2500);
  }

  private findSearchMatch(normalized: string): { label: string; route: string; keywords: string[] } | undefined {
    return this.searchItems.find(item => item.label.toLowerCase() === normalized)
      || this.searchItems.find(item => item.keywords.some(keyword => normalized === keyword))
      || this.searchItems.find(item => this.matchesSearchItem(item, normalized));
  }

  private matchesSearchItem(item: { label: string; keywords: string[] }, normalized: string): boolean {
    const label = item.label.toLowerCase();
    return label.includes(normalized)
      || item.keywords.some(keyword => keyword.includes(normalized) || normalized.includes(keyword));
  }
}
