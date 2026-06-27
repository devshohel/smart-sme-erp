import { Component, ElementRef, EventEmitter, HostListener, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationItem } from '../../../models/notification-center.model';
import { AuthService } from '../../../modules/auth/auth.service';
import { NotificationCenterService } from '../../../services/notification-center.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {

  @ViewChild('searchInput')
  searchInput?: ElementRef<HTMLInputElement>;
  @Output()
  sidebarToggle = new EventEmitter<void>();
  @Input() isSidebarCollapsed = false;

  notifications: NotificationItem[] = [];
  unreadCount = 0;
  notificationLoading = false;
  isNotificationOpen = false;
  isSearchOpen = false;
  searchMessage = '';
  searchTerm = '';
  filteredSuggestions: SearchItem[] = [];
  private searchMessageTimer: ReturnType<typeof setTimeout> | null = null;
  private unreadSubscription?: Subscription;
  private pollingTimer: ReturnType<typeof setInterval> | null = null;

  private readonly searchItems: SearchItem[] = [
    { label: 'Dashboard', route: '/dashboard', keywords: ['dashboard', 'dashbord', 'deshboard'], permissions: ['DASHBOARD_VIEW'] },
    { label: 'Notifications', route: '/notifications', keywords: ['notification', 'notifications', 'alert', 'alerts'], permissions: ['NOTIFICATION_VIEW'] },
    { label: 'All Products', route: '/products/products', keywords: ['product', 'products', 'all products'], permissions: ['PRODUCT_VIEW'] },
    { label: 'Add Product', route: '/products/add-product', keywords: ['add product'], permissions: ['PRODUCT_CREATE'] },
    { label: 'Categories', route: '/products/categories', keywords: ['category', 'categories'], anyPermissions: ['CATEGORY_VIEW', 'PRODUCT_VIEW'] },
    { label: 'Brands', route: '/products/brands', keywords: ['brand', 'brands'], anyPermissions: ['BRAND_VIEW', 'PRODUCT_VIEW'] },
    { label: 'Unit of Measures', route: '/products/uom', keywords: ['uom', 'unit', 'unit of measures'], anyPermissions: ['UOM_VIEW', 'PRODUCT_VIEW'] },
    { label: 'Stock Levels', route: '/inventory/stocks', keywords: ['stock', 'current stock', 'stock levels'], permissions: ['INVENTORY_VIEW'] },
    { label: 'Stock Movements', route: '/inventory/movements', keywords: ['stock movement', 'movement'], permissions: ['INVENTORY_VIEW'] },
    { label: 'Stock Adjustments', route: '/inventory/adjustments', keywords: ['stock adjustment', 'adjustment'], anyPermissions: ['STOCK_ADJUSTMENT_VIEW', 'STOCK_ADJUSTMENT_CREATE'] },
    { label: 'Warehouses', route: '/inventory/warehouses', keywords: ['warehouse', 'warehouses'], permissions: ['INVENTORY_VIEW'] },
    { label: 'Customer List', route: '/customers/list', keywords: ['customer', 'customers', 'customer list'], anyPermissions: ['CUSTOMER_VIEW', 'CUSTOMER_LEDGER_VIEW', 'CUSTOMER_EDIT'] },
    { label: 'Add Customer', route: '/customers/create', keywords: ['customer form', 'add customer'], permissions: ['CUSTOMER_CREATE'] },
    { label: 'Customer Aging', route: '/customers/aging', keywords: ['customer aging', 'aging'], permissions: ['CUSTOMER_AGING_VIEW'] },
    { label: 'Supplier List', route: '/suppliers/list', keywords: ['supplier', 'suppliers', 'supplier list'], anyPermissions: ['SUPPLIER_VIEW', 'SUPPLIER_LEDGER_VIEW', 'SUPPLIER_EDIT'] },
    { label: 'Add Supplier', route: '/suppliers/create', keywords: ['supplier form', 'add supplier'], permissions: ['SUPPLIER_CREATE'] },
    { label: 'Supplier Aging', route: '/suppliers/aging', keywords: ['supplier aging', 'aging'], anyPermissions: ['SUPPLIER_LEDGER_VIEW', 'SUPPLIER_VIEW'] },
    { label: 'Supplier AP Reconciliation', route: '/suppliers/ap-reconciliation', keywords: ['ap reconciliation', 'supplier reconciliation'], permissions: ['SUPPLIER_LEDGER_VIEW'] },
    { label: 'Purchase Orders', route: '/purchases/orders', keywords: ['purchase', 'purchases', 'purchas', 'parchase', 'purchase order'], permissions: ['PURCHASE_ORDER_VIEW'] },
    { label: 'Purchase Receives', route: '/purchases/receives', keywords: ['purchase invoice', 'purchase receive', 'goods receive'], anyPermissions: ['PURCHASE_RECEIVE_VIEW', 'PURCHASE_INVOICE_VIEW'] },
    { label: 'Purchase Invoices', route: '/purchases/invoices', keywords: ['purchase invoice'], permissions: ['PURCHASE_INVOICE_VIEW'] },
    { label: 'Purchase Returns', route: '/purchases/returns', keywords: ['purchase return'], permissions: ['PURCHASE_RETURN_VIEW'] },
    { label: 'Sales Orders', route: '/sales/orders', keywords: ['sales', 'sale', 'sales order', 'sale order'], permissions: ['SALES_ORDER_VIEW'] },
    { label: 'All Sales', route: '/sales', keywords: ['invoice', 'invoices', 'sales invoice', 'sales invoices'], permissions: ['SALES_INVOICE_VIEW'] },
    { label: 'Sales Returns', route: '/sales/returns', keywords: ['sales return'], permissions: ['SALES_RETURN_VIEW'] },
    { label: 'Sales Report', route: '/reports/sales', keywords: ['report', 'reports', 'sales report', 'sales reports'], permissions: ['REPORT_VIEW'] },
    { label: 'Purchase Report', route: '/reports/purchases', keywords: ['purchase report'], permissions: ['REPORT_VIEW'] },
    { label: 'Stock Report', route: '/reports/stock', keywords: ['stock report'], permissions: ['REPORT_VIEW'] },
    { label: 'Customer Due Report', route: '/reports/customer-dues', keywords: ['customer due report'], permissions: ['REPORT_VIEW'] },
    { label: 'Supplier Due Report', route: '/reports/supplier-dues', keywords: ['supplier due report'], permissions: ['REPORT_VIEW'] },
    { label: 'Profit & Loss Summary', route: '/reports/profit-loss', keywords: ['profit loss', 'profit and loss', 'p&l'], anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { label: 'Expenses', route: '/expenses', keywords: ['accounting', 'expense', 'expenses'], anyPermissions: ['EXPENSE_VIEW', 'EXPENSE_CREATE', 'EXPENSE_EDIT', 'EXPENSE_SUBMIT', 'EXPENSE_APPROVE', 'EXPENSE_REJECT', 'EXPENSE_POST', 'EXPENSE_CANCEL', 'EXPENSE_REPORT_VIEW'] },
    { label: 'Expense Categories', route: '/accounting/expense-categories', keywords: ['expense category'], permissions: ['ACCOUNTING_VIEW'] },
    { label: 'Cash Book', route: '/accounting/cash-book', keywords: ['cash book'], permissions: ['ACCOUNTING_VIEW'] },
    { label: 'Bank Book', route: '/accounting/bank-book', keywords: ['bank book'], permissions: ['ACCOUNTING_VIEW'] },
    { label: 'Journal Entries', route: '/accounting/journal-entries', keywords: ['journal', 'journal entry'], permissions: ['ACCOUNTING_VIEW'] },
    { label: 'Chart of Accounts', route: '/accounting/accounts', keywords: ['chart of accounts', 'accounts'], anyPermissions: ['ACCOUNTING_VIEW', 'ACCOUNTING_CREATE', 'ACCOUNTING_EDIT', 'BUDGET_VIEW', 'BUDGET_CREATE', 'BUDGET_EDIT'] },
    { label: 'Customer Ledger', route: '/accounting/customer-ledger', keywords: ['customer ledger'], permissions: ['ACCOUNTING_VIEW'] },
    { label: 'Supplier Ledger', route: '/accounting/supplier-ledger', keywords: ['supplier ledger'], permissions: ['ACCOUNTING_VIEW'] },
    { label: 'General Ledger', route: '/accounting/general-ledger', keywords: ['general ledger'], anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { label: 'Trial Balance', route: '/accounting/trial-balance', keywords: ['trial balance'], anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { label: 'Balance Sheet', route: '/accounting/balance-sheet', keywords: ['balance sheet'], anyPermissions: ['ACCOUNTING_VIEW', 'REPORT_VIEW'] },
    { label: 'Company Settings', route: '/settings/company', keywords: ['settings', 'company settings'], permissions: ['SETTINGS_VIEW'] },
    { label: 'Invoice Settings', route: '/settings/invoice', keywords: ['invoice settings'], permissions: ['SETTINGS_VIEW'] },
    { label: 'Tax Settings', route: '/settings/tax', keywords: ['tax settings'], permissions: ['SETTINGS_VIEW'] },
    { label: 'System Settings', route: '/settings/system', keywords: ['system settings'], permissions: ['SETTINGS_VIEW'] },
    { label: 'Users', route: '/settings/users', keywords: ['users'], permissions: ['USER_VIEW'] },
    { label: 'Roles & Permissions', route: '/settings/roles-permissions', keywords: ['roles', 'permissions', 'role permissions'], permissions: ['ROLE_VIEW'] },
    { label: 'Change Password', route: '/settings/change-password', keywords: ['change password'], permissions: [] },
    { label: 'Activity Logs', route: '/settings/activity-logs', keywords: ['activity logs'], anyPermissions: ['ACTIVITY_LOG_VIEW', 'ACTIVITY_VIEW'] },
    { label: 'Audit Logs', route: '/settings/audit-logs', keywords: ['audit logs'], permissions: ['AUDIT_VIEW'] },
    { label: 'Login History', route: '/settings/login-history', keywords: ['login history'], anyPermissions: ['LOGIN_HISTORY_VIEW', 'AUDIT_VIEW'] }
  ];

  constructor(
    private authService: AuthService,
    private notificationCenterService: NotificationCenterService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.unreadSubscription = this.notificationCenterService.unreadCount$.subscribe(count => this.unreadCount = count);
    this.loadNotifications();
    this.pollingTimer = setInterval(() => this.notificationCenterService.refreshUnreadCount(), 60000);
  }

  ngOnDestroy(): void {
    this.unreadSubscription?.unsubscribe();
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
    }
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
      ? this.searchItems.filter(item => this.canOpenSearchItem(item) && this.matchesSearchItem(item, normalized)).slice(0, 6)
      : [];
    this.isSearchOpen = this.filteredSuggestions.length > 0;
  }

  keepSearchOpen(event: MouseEvent): void {
    event.stopPropagation();
  }

  navigateSearch(route: string): void {
    const item = this.searchItems.find(searchItem => searchItem.route === route);
    if (item && !this.canOpenSearchItem(item)) {
      this.showSearchMessage('No matching section found.');
      return;
    }

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
    this.loadNotifications();
    this.isNotificationOpen = !this.isNotificationOpen;
  }

  keepNotificationsOpen(event: MouseEvent): void {
    event.stopPropagation();
  }

  navigateFromNotification(route: string): void {
    this.isNotificationOpen = false;
    this.router.navigate([route]);
  }

  openNotification(item: NotificationItem): void {
    this.isNotificationOpen = false;
    const navigate = () => {
      if (item.actionUrl) {
        this.router.navigateByUrl(item.actionUrl);
      } else {
        this.router.navigate(['/notifications', item.id], { state: { notification: item } });
      }
    };

    if (!item.read) {
      item.read = true;
      this.notificationCenterService.markAsRead(item.id).subscribe({
        next: updated => {
          Object.assign(item, updated);
          navigate();
        },
        error: () => navigate()
      });
      return;
    }
    navigate();
  }

  markAllNotificationsRead(event: MouseEvent): void {
    event.stopPropagation();
    this.notificationCenterService.markAllAsRead().subscribe({
      next: () => this.notifications = this.notifications.map(item => ({ ...item, read: true })),
      error: () => undefined
    });
  }

  openNotificationHistory(event: MouseEvent): void {
    event.stopPropagation();
    this.isNotificationOpen = false;
    this.router.navigate(['/notifications']);
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
    return this.unreadCount;
  }

  get hasNotifications(): boolean {
    return this.notifications.length > 0;
  }

  private loadNotifications(): void {
    this.notificationLoading = true;
    this.notificationCenterService.getUnreadCount().subscribe({ error: () => undefined });
    this.notificationCenterService.getPreview().subscribe({
      next: notifications => {
        this.notifications = notifications;
        this.notificationLoading = false;
      },
      error: () => {
        this.notifications = [];
        this.notificationLoading = false;
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
    const visibleItems = this.searchItems.filter(item => this.canOpenSearchItem(item));
    return visibleItems.find(item => item.label.toLowerCase() === normalized)
      || visibleItems.find(item => item.keywords.some(keyword => normalized === keyword))
      || visibleItems.find(item => this.matchesSearchItem(item, normalized));
  }

  private matchesSearchItem(item: { label: string; keywords: string[] }, normalized: string): boolean {
    const label = item.label.toLowerCase();
    return label.includes(normalized)
      || item.keywords.some(keyword => keyword.includes(normalized) || normalized.includes(keyword));
  }

  private canOpenSearchItem(item: SearchItem): boolean {
    if (item.permissions?.length) {
      return this.authService.hasAllPermissions(item.permissions);
    }
    if (item.anyPermissions?.length) {
      return this.authService.hasAnyPermission(item.anyPermissions);
    }
    return true;
  }
}

interface SearchItem {
  label: string;
  route: string;
  keywords: string[];
  permissions?: string[];
  anyPermissions?: string[];
}
