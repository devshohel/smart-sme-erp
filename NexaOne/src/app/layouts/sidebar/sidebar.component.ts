/**
 * SIDEBAR COMPONENT - Enhanced Layout Integration
 *
 * @description
 * Enhanced sidebar component with LayoutService integration.
 * Maintains full backward compatibility while adding responsive
 * behavior and enterprise-grade state management.
 *
 * @phase 2
 * - LayoutService integration
 * - Responsive sidebar behavior
 * - Mobile sidebar support
 * - Backward compatible with existing functionality
 *
 * @version 2.0.0
 * @since 2026-05-10
 */

import { Component, OnInit, OnDestroy, HostBinding } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject, combineLatest } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Layout Service Import
import { LayoutService } from '../../core/services/layout.service';

/**
 * Sidebar Component
 *
 * @description
 * Main sidebar navigation component with responsive behavior.
 * Integrates with LayoutService for enterprise-grade state management
 * while maintaining all existing functionality.
 *
 * @enhancements
 * - LayoutService integration for state management
 * - Responsive sidebar behavior (desktop/mobile)
 * - Mobile sidebar overlay support
 * - Active route highlighting
 * - Proper cleanup with OnDestroy
 *
 * @backwardCompatibility
 * - Keeps existing toggleInventory() method working
 * - Keeps existing template structure unchanged
 * - All existing CSS classes remain functional
 *
 * @example
 * <app-sidebar></app-sidebar>
 */
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {
  // ========== EXISTING PROPERTIES (KEEP FOR BACKWARD COMPATIBILITY) ==========

  /**
   * Existing property for inventory dropdown
   * @deprecated Still functional for backward compatibility
   */
  isInventoryOpen: boolean = false;

  // ========== NEW PROPERTIES (LAYOUT SERVICE INTEGRATION) ==========

  /**
   * Destroy subject for cleanup
   * @private
   */
  private readonly destroy$ = new Subject<void>();

  /**
   * Navigation timer reference (for cleanup)
   * @private
   */
  private navigationTimer: any;

  /**
   * Sidebar expanded state (desktop)
   * @type {boolean}
   */
  sidebarExpanded: boolean = true;

  /**
   * Sidebar collapsed state (desktop)
   * @type {boolean}
   */
  sidebarCollapsed: boolean = false;

  /**
   * Mobile sidebar open state
   * @type {boolean}
   */
  mobileSidebarOpen: boolean = false;

  /**
   * Current device type
   * @type {boolean}
   */
  isMobile: boolean = false;

  /**
   * Current route path
   * @type {string}
   */
  currentRoute: string = '';

  // ========== CONSTRUCTOR ==========

  /**
   * Constructor with dependency injection
   *
   * @param layoutService - Layout state management service
   * @param router - Angular router for active route detection
   */
  constructor(
    public layoutService: LayoutService,
    private router: Router
  ) {
    // Layout service is public for template access
  }

  // ========== EXISTING METHODS (KEEP FOR BACKWARD COMPATIBILITY) ==========

  /**
   * Existing inventory toggle method
   *
   * @description
   * Maintains backward compatibility with existing template.
   * Still works exactly as before.
   *
   * @deprecated Use toggleMenu() for new menu items
   */
  toggleInventory(): void {
    this.isInventoryOpen = !this.isInventoryOpen;
  }

  // ========== NEW METHODS (LAYOUT SERVICE INTEGRATION) ==========

  /**
   * Component initialization
   *
   * @description
   * Subscribe to layout state changes and router events.
   * Updates component properties when state changes.
   */
  ngOnInit(): void {
    // Subscribe to sidebar expanded state
    this.layoutService.sidebarExpanded$
      .pipe(takeUntil(this.destroy$))
      .subscribe(expanded => {
        this.sidebarExpanded = expanded;
        this.sidebarCollapsed = !expanded;
      });

    // Subscribe to mobile sidebar state
    this.layoutService.mobileSidebarOpen$
      .pipe(takeUntil(this.destroy$))
      .subscribe(mobileOpen => {
        this.mobileSidebarOpen = mobileOpen;
      });

    // Subscribe to mobile breakpoint state
    this.layoutService.isMobile$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isMobile => {
        this.isMobile = isMobile;
        // Close inventory dropdown when switching to mobile
        if (isMobile) {
          this.isInventoryOpen = false;
        }
      });

    // Subscribe to router events for active route highlighting
    this.router.events.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.currentRoute = this.router.url;

      // Close mobile sidebar after navigation (with proper cleanup)
      if (this.mobileSidebarOpen) {
        // Clear any existing timer
        if (this.navigationTimer) {
          clearTimeout(this.navigationTimer);
        }

        // Set new timer with proper reference
        this.navigationTimer = setTimeout(() => {
          this.layoutService.closeMobileSidebar();
          this.navigationTimer = null;
        }, 150); // Small delay for smooth transition
      }
    });

    // Initialize current route
    this.currentRoute = this.router.url;
  }

  /**
   * Toggle sidebar (delegates to LayoutService)
   *
   * @description
   * Toggles sidebar state based on current device type.
   * On mobile: toggles overlay
   * On desktop: toggles expanded/collapsed
   *
   * @example
   * <button (click)="onSidebarToggle()">Toggle</button>
   */
  onSidebarToggle(): void {
    this.layoutService.toggleSidebar();
  }

  /**
   * Expand sidebar (desktop only)
   *
   * @description
   * Forces sidebar to expanded state on desktop.
   * No effect on mobile devices.
   *
   * @example
   * <button (click)="expandSidebar()">Expand</button>
   */
  expandSidebar(): void {
    this.layoutService.expandSidebar();
  }

  /**
   * Collapse sidebar (desktop only)
   *
   * @description
   * Forces sidebar to collapsed state (icons only) on desktop.
   * No effect on mobile devices.
   *
   * @example
   * <button (click)="collapseSidebar()">Collapse</button>
   */
  collapseSidebar(): void {
    this.layoutService.collapseSidebar();
  }

  /**
   * Close mobile sidebar
   *
   * @description
   * Closes mobile sidebar overlay.
   * Automatically called after navigation on mobile.
   *
   * @example
   * <div class="overlay" (click)="closeMobileSidebar()"></div>
   */
  closeMobileSidebar(): void {
    this.layoutService.closeMobileSidebar();
  }

  /**
   * Enhanced menu toggle (supports multiple menus)
   *
   * @description
   * Enhanced version of toggleInventory() that can handle
   * multiple menu groups. Maintains backward compatibility
   * with existing toggleInventory() method.
   *
   * @param menuId - Unique identifier for the menu group
   * @param event - Click event (optional)
   *
   * @example
   * <a (click)="toggleMenu('inventory')">Inventory</a>
   */
  toggleMenu(menuId: string, event?: Event): void {
    // Prevent event propagation if provided
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    // Maintain backward compatibility with existing toggleInventory()
    if (menuId === 'inventory') {
      this.toggleInventory();
    }
  }

  /**
   * Check if route is currently active
   *
   * @description
   * Checks if the given route is currently active.
   * Supports exact matching and partial matching for child routes.
   *
   * @param route - Route path to check
   * @param exact - Whether to match exact route (default: false)
   * @returns true if route is active
   *
   * @example
   * <a [ngClass]="{active: isRouteActive('/dashboard')}">Dashboard</a>
   */
  isRouteActive(route: string, exact: boolean = false): boolean {
    if (exact) {
      return this.currentRoute === route;
    }
    return this.currentRoute.startsWith(route);
  }

  /**
   * Get CSS classes for sidebar container
   *
   * @description
   * Returns dynamic CSS classes based on sidebar state.
   * Used in template for responsive behavior.
   *
   * @returns Object with CSS class names as keys
   *
   * @example
   * <div [ngClass]="getSidebarClasses()">
   */
  getSidebarClasses(): { [key: string]: boolean } {
    return {
      'expanded': this.sidebarExpanded,
      'collapsed': this.sidebarCollapsed,
      'mobile-open': this.mobileSidebarOpen,
      'is-mobile': this.isMobile,
      'is-desktop': !this.isMobile
    };
  }

  /**
   * Handle keyboard navigation
   *
   * @description
   * Enables keyboard navigation for accessibility.
   * Supports Escape key to close mobile sidebar.
   *
   * @param event - Keyboard event
   *
   * @example
   * <div (keydown)="onKeyDown($event)">
   */
  onKeyDown(event: KeyboardEvent): void {
    // Close mobile sidebar on Escape key
    if (event.key === 'Escape' && this.mobileSidebarOpen) {
      this.closeMobileSidebar();
    }

    // Toggle sidebar on Ctrl/Cmd + B (common shortcut)
    if ((event.ctrlKey || event.metaKey) && event.key === 'b') {
      event.preventDefault();
      this.onSidebarToggle();
    }
  }

  /**
   * Cleanup on component destruction
   *
   * @description
   * Implements OnDestroy for proper cleanup.
   * Prevents memory leaks by completing all subscriptions.
   */
  ngOnDestroy(): void {
    // Clear navigation timer if exists
    if (this.navigationTimer) {
      clearTimeout(this.navigationTimer);
      this.navigationTimer = null;
    }

    this.destroy$.next();
    this.destroy$.complete();
  }

  // ========== HOST BINDINGS (DYNAMIC CSS CLASSES) ==========

  /**
   * Dynamic CSS classes for host element
   *
   * @description
   * Applies CSS classes to the component host element
   * based on sidebar state. Enables parent components
   * to react to sidebar state changes.
   */
  @HostBinding('class') get hostClasses(): string {
    const classes = ['sidebar'];

    if (this.sidebarExpanded) classes.push('expanded');
    if (this.sidebarCollapsed) classes.push('collapsed');
    if (this.mobileSidebarOpen) classes.push('mobile-open');
    if (this.isMobile) classes.push('is-mobile');
    else classes.push('is-desktop');

    return classes.join(' ');
  }

  @HostBinding('attr.role') role = 'navigation';

  @HostBinding('attr.aria-label') ariaLabel = 'Main navigation';
}