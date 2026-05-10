/**
 * NAVBAR COMPONENT - Enhanced Layout Integration
 *
 * @description
 * Enhanced navbar component with LayoutService integration.
 * Provides sidebar toggle functionality and responsive behavior
 * while maintaining existing design and features.
 *
 * @phase 3
 * - LayoutService integration
 * - Sidebar toggle button (hamburger menu)
 * - Search functionality
 * - User menu dropdown
 * - Notification support
 *
 * @backwardCompatibility
 * - Keeps existing navbar design unchanged
 * - All existing features preserved
 * - Template structure maintained
 *
 * @version 2.0.0
 * @since 2026-05-10
 */

import { Component, OnInit, OnDestroy, HostListener, HostBinding, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject, fromEvent } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

// Layout Service Import
import { LayoutService } from '../../core/services/layout.service';

/**
 * Notification interface
 */
interface Notification {
  id: string;
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
  type: 'info' | 'warning' | 'error' | 'success';
}

/**
 * Navbar Component
 *
 * @description
 * Top navigation bar with sidebar toggle, search, notifications,
 * and user menu. Integrates with LayoutService for coordinated
 * sidebar control.
 *
 * @enhancements
 * - LayoutService integration
 * - Sidebar toggle button (hamburger)
 * - Responsive behavior
 * - Click-outside detection
 * - Keyboard shortcuts
 *
 * @example
 * <app-navbar></app-navbar>
 */
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  // ========== EXISTING PROPERTIES (PRESERVED) ==========

  /**
   * User information (hardcoded for now, will be dynamic later)
   */
  userName: string = 'Shohel';
  userRole: string = 'Admin';
  userAvatar: string = '/assets/images/profile-user.png';

  // ========== NEW PROPERTIES (LAYOUT SERVICE INTEGRATION) ==========

  /**
   * Destroy subject for cleanup
   * @private
   */
  private readonly destroy$ = new Subject<void>();

  /**
   * Sidebar expanded state (desktop)
   * @type {boolean}
   */
  sidebarExpanded: boolean = true;

  /**
   * Mobile breakpoint state
   * @type {boolean}
   */
  isMobile: boolean = false;

  /**
   * Search query text
   * @type {string}
   */
  searchQuery: string = '';

  /**
   * Search input focused state
   * @type {boolean}
   */
  searchFocused: boolean = false;

  /**
   * User menu open state
   * @type {boolean}
   */
  userMenuOpen: boolean = false;

  /**
   * Notifications panel open state
   * @type {boolean}
   */
  notificationsOpen: boolean = false;

  /**
   * Notification count badge
   * @type {number}
   */
  notificationCount: number = 0;

  /**
   * Notifications list
   * @type {Notification[]}
   */
  notifications: Notification[] = [];

  // ========== CONSTRUCTOR ==========

  /**
   * Constructor with dependency injection
   *
   * @param layoutService - Layout state management service
   * @param router - Angular router for navigation
   * @param elementRef - Element reference for click detection
   */
  constructor(
    public layoutService: LayoutService,
    private router: Router,
    private elementRef: ElementRef
  ) {
    // Layout service is public for template access
  }

  // ========== LIFECYCLE HOOKS ==========

  /**
   * Component initialization
   *
   * @description
   * Subscribe to layout state changes and setup event listeners.
   */
  ngOnInit(): void {
    // Subscribe to sidebar expanded state
    this.layoutService.sidebarExpanded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(expanded => {
        this.sidebarExpanded = expanded;
      });

    // Subscribe to mobile breakpoint state
    this.layoutService.isMobile$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isMobile => {
        this.isMobile = isMobile;
      });

    // Setup click-outside listener for dropdowns
    this.setupClickOutsideListener();

    // Load sample notifications (will be replaced with API call later)
    this.loadNotifications();
  }

  /**
   * Component cleanup
   *
   * @description
   * Prevent memory leaks by completing all subjects.
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ========== SIDEBAR TOGGLE METHODS ==========

  /**
   * Toggle sidebar button click handler
   *
   * @description
   * Main sidebar toggle functionality. Delegates to LayoutService
   * which handles the logic based on current device type.
   *
   * @example
   * <button (click)="onSidebarToggle()">Toggle</button>
   */
  onSidebarToggle(): void {
    this.layoutService.toggleSidebar();
  }

  /**
   * Force expand sidebar (desktop)
   *
   * @description
   * Forces sidebar to expanded state on desktop.
   *
   * @example
   * <button (click)="expandSidebar()">Expand</button>
   */
  expandSidebar(): void {
    this.layoutService.expandSidebar();
  }

  /**
   * Force collapse sidebar (desktop)
   *
   * @description
   * Forces sidebar to collapsed state on desktop.
   *
   * @example
   * <button (click)="collapseSidebar()">Collapse</button>
   */
  collapseSidebar(): void {
    this.layoutService.collapseSidebar();
  }

  // ========== SEARCH FUNCTIONALITY ==========

  /**
   * Search form submission handler
   *
   * @description
   * Performs search when user submits the search form.
   * Navigates to search page or filters current page.
   *
   * @example
   * <form (ngSubmit)="onSearch()">
   */
  onSearch(): void {
    if (this.searchQuery.trim()) {
      // Navigate to search page with query parameter
      this.router.navigate(['/search'], {
        queryParams: { q: this.searchQuery.trim() }
      });

      // Clear search after navigation
      this.searchQuery = '';
    }
  }

  /**
   * Search input keydown handler
   *
   * @description
   * Handles keyboard events in search input.
   * Triggers search on Enter key.
   *
   * @param event - Keyboard event
   *
   * @example
   * <input (keydown)="onSearchKeydown($event)">
   */
  onSearchKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.onSearch();
    }

    // Clear search on Escape
    if (event.key === 'Escape') {
      this.searchQuery = '';
      this.searchFocused = false;
    }
  }

  /**
   * Focus search input
   *
   * @description
   * Programmatically focus search input.
   * Useful for keyboard shortcuts.
   *
   * @example
   * <button (click)="focusSearch()">Search</button>
   */
  focusSearch(): void {
    const searchInput = this.elementRef.nativeElement.querySelector(
      'input[type="text"]'
    ) as HTMLInputElement;
    if (searchInput) {
      searchInput.focus();
      this.searchFocused = true;
    }
  }

  // ========== USER MENU FUNCTIONALITY ==========

  /**
   * Toggle user dropdown menu
   *
   * @description
   * Toggles user profile dropdown menu.
   * Closes other dropdowns when opening.
   *
   * @example
   * <div (click)="toggleUserMenu()">User Profile</div>
   */
  toggleUserMenu(): void {
    this.userMenuOpen = !this.userMenuOpen;

    // Close notifications when user menu opens
    if (this.userMenuOpen) {
      this.notificationsOpen = false;
    }
  }

  /**
   * Close user dropdown menu
   *
   * @description
   * Closes user dropdown menu.
   * Called when clicking outside or after selection.
   *
   * @example
   * this.closeUserMenu();
   */
  closeUserMenu(): void {
    this.userMenuOpen = false;
  }

  /**
   * Navigate to user profile
   *
   * @description
   * Navigate to user profile page and close menu.
   *
   * @example
   * <a (click)="goToProfile()">Profile</a>
   */
  goToProfile(): void {
    this.closeUserMenu();
    this.router.navigate(['/profile']);
  }

  /**
   * Navigate to settings
   *
   * @description
   * Navigate to settings page and close menu.
   *
   * @example
   * <a (click)="goToSettings()">Settings</a>
   */
  goToSettings(): void {
    this.closeUserMenu();
    this.router.navigate(['/settings']);
  }

  /**
   * Handle logout action
   *
   * @description
   * Logs out user and redirects to login page.
   * Closes menu before navigation.
   *
   * @example
   * <button (click)="onLogout()">Logout</button>
   */
  onLogout(): void {
    this.closeUserMenu();

    // TODO: Implement actual logout logic
    // - Call auth service logout method
    // - Clear tokens
    // - Clear user data
    // - Redirect to login

    this.router.navigate(['/auth/login']);
  }

  // ========== NOTIFICATION FUNCTIONALITY ==========

  /**
   * Toggle notifications panel
   *
   * @description
   * Toggles notifications dropdown panel.
   * Closes other dropdowns when opening.
   *
   * @example
   * <button (click)="toggleNotifications()">Notifications</button>
   */
  toggleNotifications(): void {
    this.notificationsOpen = !this.notificationsOpen;

    // Close user menu when notifications open
    if (this.notificationsOpen) {
      this.userMenuOpen = false;
      // Mark all as read when opening
      this.markAllAsRead();
    }
  }

  /**
   * Close notifications panel
   *
   * @description
   * Closes notifications dropdown panel.
   *
   * @example
   * this.closeNotifications();
   */
  closeNotifications(): void {
    this.notificationsOpen = false;
  }

  /**
   * Mark notification as read
   *
   * @description
   * Marks a specific notification as read.
   *
   * @param notification - Notification to mark as read
   *
   * @example
   * <div (click)="markAsRead(notification)">
   */
  markAsRead(notification: Notification): void {
    notification.read = true;
    this.updateNotificationCount();
  }

  /**
   * Mark all notifications as read
   *
   * @description
   * Marks all notifications as read.
   *
   * @example
   * <button (click)="markAllAsRead()">Mark all read</button>
   */
  markAllAsRead(): void {
    this.notifications.forEach(notification => {
      notification.read = true;
    });
    this.notificationCount = 0;
  }

  /**
   * Clear all notifications
   *
   * @description
   * Removes all notifications.
   *
   * @example
   * <button (click)="clearNotifications()">Clear all</button>
   */
  clearNotifications(): void {
    this.notifications = [];
    this.notificationCount = 0;
    this.closeNotifications();
  }

  // ========== HELPER METHODS ==========

  /**
   * Setup click-outside listener
   *
   * @description
   * Detects clicks outside dropdown menus to close them.
   * Uses RxJS fromEvent for proper cleanup.
   *
   * @private
   */
  private setupClickOutsideListener(): void {
    fromEvent(document, 'click')
      .pipe(
        takeUntil(this.destroy$),
        filter((event: Event) => {
          // Check if click is outside navbar component
          return !this.elementRef.nativeElement.contains(event.target as Node);
        })
      )
      .subscribe(() => {
        // Close dropdowns when clicking outside
        if (this.userMenuOpen) {
          this.closeUserMenu();
        }
        if (this.notificationsOpen) {
          this.closeNotifications();
        }
      });
  }

  /**
   * Load notifications (placeholder)
   *
   * @description
   * Loads notifications from API.
   * Currently uses sample data.
   *
   * @private
   */
  private loadNotifications(): void {
    // TODO: Replace with actual API call
    // this.notificationService.getNotifications().subscribe(...)

    // Sample notifications for now
    this.notifications = [
      {
        id: '1',
        title: 'New Order',
        message: 'You have received a new order #1234',
        timestamp: new Date(Date.now() - 300000), // 5 minutes ago
        read: false,
        type: 'success'
      },
      {
        id: '2',
        title: 'Low Stock Alert',
        message: 'Product "Monitor" is running low on stock',
        timestamp: new Date(Date.now() - 3600000), // 1 hour ago
        read: false,
        type: 'warning'
      },
      {
        id: '3',
        title: 'Payment Received',
        message: 'Payment of $1,250 received from John Doe',
        timestamp: new Date(Date.now() - 7200000), // 2 hours ago
        read: true,
        type: 'info'
      }
    ];

    this.updateNotificationCount();
  }

  /**
   * Update notification count
   *
   * @description
   * Calculates unread notification count for badge.
   *
   * @private
   */
  private updateNotificationCount(): void {
    this.notificationCount = this.notifications.filter(
      notification => !notification.read
    ).length;
  }

  /**
   * Format notification time for display
   *
   * @description
   * Returns human-readable time string.
   *
   * @param notification - Notification with timestamp
   * @returns Formatted time string
   *
   * @example
   * formatTime(notification) // "5 minutes ago"
   */
  formatTime(notification: Notification): string {
    const now = new Date();
    const diff = now.getTime() - notification.timestamp.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  }

  // ========== KEYBOARD SHORTCUTS ==========

  /**
   * Global keyboard shortcuts
   *
   * @description
   * Handles keyboard shortcuts for navbar functionality.
   * Called from host binding.
   *
   * @param event - Keyboard event
   *
   * @example
   * Ctrl/Cmd + K: Focus search
   * Ctrl/Cmd + B: Toggle sidebar
   */
  @HostListener('window:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    // Ctrl/Cmd + K: Focus search
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
      event.preventDefault();
      this.focusSearch();
    }

    // Ctrl/Cmd + B: Toggle sidebar (delegated to LayoutService)
    if ((event.ctrlKey || event.metaKey) && event.key === 'b') {
      event.preventDefault();
      this.onSidebarToggle();
    }

    // Escape: Close dropdowns
    if (event.key === 'Escape') {
      if (this.userMenuOpen) {
        this.closeUserMenu();
      }
      if (this.notificationsOpen) {
        this.closeNotifications();
      }
      if (this.searchFocused) {
        this.searchQuery = '';
        this.searchFocused = false;
      }
    }
  }

  // ========== HOST BINDINGS ==========

  /**
   * Dynamic CSS classes for host element
   */
  @HostBinding('class') get hostClasses(): string {
    const classes = ['navbar'];

    if (this.isMobile) classes.push('is-mobile');
    else classes.push('is-desktop');

    return classes.join(' ');
  }

  @HostBinding('attr.role') role = 'banner';

  @HostBinding('attr.aria-label') ariaLabel = 'Main navigation';
}