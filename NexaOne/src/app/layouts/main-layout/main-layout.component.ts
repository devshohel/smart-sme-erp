/**
 * MAIN LAYOUT COMPONENT - Enhanced Layout Integration
 *
 * @description
 * Enhanced main layout component with LayoutService integration.
 * Provides responsive container behavior and coordinates
 * sidebar/navbar interactions.
 *
 * @phase 3
 * - LayoutService integration
 * - Responsive container classes
 * - Dynamic margin adjustments
 * - Mobile overlay support
 *
 * @backwardCompatibility
 * - Keeps existing layout structure
 * - All child components work unchanged
 * - Template structure maintained
 *
 * @version 2.0.0
 * @since 2026-05-10
 */

import { Component, OnInit, OnDestroy, HostBinding } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Layout Service Import
import { LayoutService } from '../../core/services/layout.service';

/**
 * Main Layout Component
 *
 * @description
 * Main application layout component that hosts sidebar,
 * navbar, and router outlet. Provides responsive container
 * behavior with dynamic margin adjustments based on sidebar state.
 *
 * @enhancements
 * - LayoutService integration for state tracking
 * - Dynamic CSS classes for responsive behavior
 * - Mobile overlay support
 * - Proper cleanup and memory management
 *
 * @example
 * <app-main-layout></app-main-layout>
 */
@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit, OnDestroy {
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
   * Mobile breakpoint state
   * @type {boolean}
   */
  isMobile: boolean = false;

  // ========== CONSTRUCTOR ==========

  /**
   * Constructor with dependency injection
   *
   * @param layoutService - Layout state management service
   */
  constructor(
    public layoutService: LayoutService
  ) {
    // Layout service is public for template access
  }

  // ========== LIFECYCLE HOOKS ==========

  /**
   * Component initialization
   *
   * @description
   * Subscribe to layout state changes and update component properties.
   * Ensures layout stays in sync with sidebar state changes.
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
      });
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

  // ========== COMPUTED PROPERTIES ==========

  /**
   * Get CSS classes for main layout container
   *
   * @description
   * Returns dynamic CSS classes based on layout state.
   * Used in template for responsive behavior.
   *
   * @returns Object with CSS class names as keys
   *
   * @example
   * <div [ngClass]="getContainerClasses()">
   */
  getContainerClasses(): { [key: string]: boolean } {
    return {
      'sidebar-expanded': this.sidebarExpanded,
      'sidebar-collapsed': this.sidebarCollapsed,
      'mobile-sidebar-open': this.mobileSidebarOpen,
      'is-mobile': this.isMobile,
      'is-desktop': !this.isMobile
    };
  }

  /**
   * Get CSS classes for main body container
   *
   * @description
   * Returns dynamic CSS classes for the content area.
   * Adjusts based on sidebar state.
   *
   * @returns Object with CSS class names as keys
   *
   * @example
   * <div [ngClass]="getBodyClasses()">
   */
  getBodyClasses(): { [key: string]: boolean } {
    return {
      'sidebar-expanded': this.sidebarExpanded,
      'sidebar-collapsed': this.sidebarCollapsed,
      'mobile-open': this.mobileSidebarOpen
    };
  }

  /**
   * Check if mobile overlay should be shown
   *
   * @description
   * Returns true when mobile sidebar overlay is visible.
   *
   * @returns true if mobile overlay should be shown
   */
  showMobileOverlay(): boolean {
    return this.isMobile && this.mobileSidebarOpen;
  }

  /**
   * Close mobile sidebar overlay
   *
   * @description
   * Closes mobile sidebar when overlay is clicked.
   * Called from template.
   *
   * @example
   * <div class="overlay" (click)="closeOverlay()"></div>
   */
  closeOverlay(): void {
    if (this.isMobile && this.mobileSidebarOpen) {
      this.layoutService.closeMobileSidebar();
    }
  }

  // ========== HOST BINDINGS ==========

  /**
   * Dynamic CSS classes for host element
   *
   * @description
   * Applies CSS classes to the component host element
   * based on layout state.
   */
  @HostBinding('class') get hostClasses(): string {
    const classes = ['main-layout'];

    if (this.sidebarExpanded) classes.push('sidebar-expanded');
    if (this.sidebarCollapsed) classes.push('sidebar-collapsed');
    if (this.mobileSidebarOpen) classes.push('mobile-sidebar-open');
    if (this.isMobile) classes.push('is-mobile');
    else classes.push('is-desktop');

    return classes.join(' ');
  }
}
