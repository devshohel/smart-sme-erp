/**
 * LAYOUT SERVICE - Centralized Layout State Management
 *
 * @description
 * Enterprise-grade service for managing application layout state.
 * Provides reactive state management using RxJS BehaviorSubjects
 * for sidebar control, responsive behavior, and screen tracking.
 *
 * @architecture
 * - Uses BehaviorSubject for state (always has a value)
 * - Exposes Observables for component subscriptions
 * - Implements automatic cleanup to prevent memory leaks
 * - Supports dependency injection throughout the application
 *
 * @usage
 * constructor(private layoutService: LayoutService) {
 *   this.sidebarExpanded$ = this.layoutService.sidebarExpanded$;
 * }
 *
 * @version 1.0.0
 * @since 2026-05-10
 */

import { Injectable, OnDestroy, inject } from '@angular/core';
import { BehaviorSubject, Observable, Subject, fromEvent, merge } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil, map } from 'rxjs/operators';

// Model imports
import {
  SidebarState,
  ScreenSize,
  LayoutState,
  LayoutEvent,
  LayoutEventType,
  UserPreferences
} from '../models/layout.model';

// Configuration imports
import {
  LAYOUT_CONFIG,
  BreakpointUtils
} from '../config/responsive.config';

/**
 * Layout Service
 *
 * @description
 * Centralized state management for the entire layout system.
 * Components subscribe to observables to react to layout changes.
 *
 * @implements OnDestroy - Ensures proper cleanup of subscriptions
 *
 * @example
 * // In a component:
 * constructor(private layoutService: LayoutService) {
 *   this.isMobile$ = this.layoutService.isMobile$;
 * }
 *
 * // Toggle sidebar:
 * this.layoutService.toggleSidebar();
 */
@Injectable({
  providedIn: 'root'
})
export class LayoutService implements OnDestroy {
  /**
   * Configuration reference
   * @private
   */
  private readonly config = LAYOUT_CONFIG;

  /**
   * Destroy subject for cleanup
   * @private
   */
  private readonly destroy$ = new Subject<void>();

  /**
   * Event emitter for layout changes
   * @private
   */
  private readonly eventEmitter = new Subject<LayoutEvent>();

  /**
   * Resize in progress flag (prevents conflicting resize operations)
   * @private
   */
  private resizeInProgress: boolean = false;

  /**
   * Sidebar state BehaviorSubject
   * @private
   */
  private readonly sidebarState$ = new BehaviorSubject<SidebarState>({
    expanded: true,
    collapsed: false,
    mobileOpen: false,
    currentWidth: this.config.sidebar.expandedWidth
  });

  /**
   * Screen size BehaviorSubject
   * @private
   */
  private readonly screenSizeSubject$ = new BehaviorSubject<ScreenSize>({
    isMobile: false,
    isTablet: false,
    isDesktop: true,
    width: 1920,
    height: 1080
  });

  /**
   * User preferences BehaviorSubject
   * @private
   */
  private readonly userPreferencesSubject$ = new BehaviorSubject<UserPreferences>({
    sidebarExpanded: true,
    theme: 'light',
    language: 'en',
    density: 'comfortable'
  });

  // ========== PUBLIC OBSERVABLES ==========

  /**
   * Observable of sidebar expanded state
   *
   * @description
   * Emits true when sidebar is expanded on desktop
   *
   * @example
   * this.layoutService.sidebarExpanded$.subscribe(expanded => {
   *   console.log('Sidebar expanded:', expanded);
   * });
   */
  readonly sidebarExpanded$: Observable<boolean> = this.sidebarState$.pipe(
    map(state => state.expanded)
  );

  /**
   * Observable of sidebar collapsed state
   *
   * @description
   * Emits true when sidebar is collapsed to icons on desktop
   *
   * @example
   * this.layoutService.sidebarCollapsed$.subscribe(collapsed => {
   *   console.log('Sidebar collapsed:', collapsed);
   * });
   */
  readonly sidebarCollapsed$: Observable<boolean> = this.sidebarState$.pipe(
    map(state => state.collapsed)
  );

  /**
   * Observable of mobile sidebar open state
   *
   * @description
   * Emits true when mobile sidebar overlay is open
   *
   * @example
   * this.layoutService.mobileSidebarOpen$.subscribe(open => {
   *   console.log('Mobile sidebar open:', open);
   * });
   */
  readonly mobileSidebarOpen$: Observable<boolean> = this.sidebarState$.pipe(
    map(state => state.mobileOpen)
  );

  /**
   * Observable of current screen size
   *
   * @description
   * Emits screen size information on window resize
   *
   * @example
   * this.layoutService.screenSize$.subscribe(size => {
   *   console.log('Screen size:', size);
   * });
   */
  readonly screenSize$: Observable<ScreenSize> = this.screenSizeSubject$.asObservable();

  /**
   * Observable of mobile breakpoint state
   *
   * @description
   * Emits true when viewport is in mobile range
   *
   * @example
   * this.layoutService.isMobile$.subscribe(isMobile => {
   *   console.log('Is mobile:', isMobile);
   * });
   */
  readonly isMobile$: Observable<boolean> = this.screenSizeSubject$.pipe(
    map(size => size.isMobile),
    distinctUntilChanged()
  );

  /**
   * Observable of tablet breakpoint state
   *
   * @description
   * Emits true when viewport is in tablet range
   *
   * @example
   * this.layoutService.isTablet$.subscribe(isTablet => {
   *   console.log('Is tablet:', isTablet);
   * });
   */
  readonly isTablet$: Observable<boolean> = this.screenSizeSubject$.pipe(
    map(size => size.isTablet),
    distinctUntilChanged()
  );

  /**
   * Observable of desktop breakpoint state
   *
   * @description
   * Emits true when viewport is in desktop range
   *
   * @example
   * this.layoutService.isDesktop$.subscribe(isDesktop => {
   *   console.log('Is desktop:', isDesktop);
   * });
   */
  readonly isDesktop$: Observable<boolean> = this.screenSizeSubject$.pipe(
    map(size => size.isDesktop),
    distinctUntilChanged()
  );

  /**
   * Observable of layout events
   *
   * @description
   * Emits events when layout state changes
   *
   * @example
   * this.layoutService.events$.subscribe(event => {
   *   console.log('Layout event:', event.type, event.data);
   * });
   */
  readonly events$: Observable<LayoutEvent> = this.eventEmitter.asObservable();

  /**
   * Observable of user preferences
   *
   * @description
   * Emits user preferences for personalized layout
   *
   * @example
   * this.layoutService.userPreferences$.subscribe(pref => {
   *   console.log('User preferences:', pref);
   * });
   */
  readonly userPreferences$: Observable<UserPreferences> = this.userPreferencesSubject$.asObservable();

  // ========== CONSTRUCTOR ==========

  constructor() {
    this.initializeWindowSize();
    this.setupWindowResizeListener();
    this.loadUserPreferences();
    this.validateStateAfterInit();
    this.emitEvent('resize', { message: 'Layout service initialized' });
  }

  // ========== PUBLIC METHODS - SIDEBAR CONTROL ==========

  /**
   * Toggle sidebar state
   *
   * @description
   * Toggles sidebar between expanded and collapsed states.
   * On mobile, toggles the overlay instead.
   *
   * @example
   * this.layoutService.toggleSidebar();
   */
  toggleSidebar(): void {
    const currentSize = this.screenSizeSubject$.value;
    const currentState = this.sidebarState$.value;

    if (currentSize.isMobile) {
      // On mobile, toggle overlay
      this.toggleMobileSidebar();
    } else {
      // On desktop, toggle expanded/collapsed
      const newState = !currentState.expanded;
      this.setSidebarExpanded(newState);
      this.emitEvent('sidebar.toggle', {
        from: currentState.expanded ? 'expanded' : 'collapsed',
        to: newState ? 'expanded' : 'collapsed'
      });
    }
  }

  /**
   * Expand sidebar (desktop)
   *
   * @description
   * Forces sidebar to expanded state. No effect on mobile.
   *
   * @example
   * this.layoutService.expandSidebar();
   */
  expandSidebar(): void {
    const currentSize = this.screenSizeSubject$.value;

    if (!currentSize.isMobile) {
      this.updateSidebarState({
        expanded: true,
        collapsed: false,
        mobileOpen: false,
        currentWidth: this.config.sidebar.expandedWidth
      });
      this.emitEvent('sidebar.expand');
      this.saveUserPreferences();
    }
  }

  /**
   * Collapse sidebar (desktop)
   *
   * @description
   * Forces sidebar to collapsed state (icons only). No effect on mobile.
   *
   * @example
   * this.layoutService.collapseSidebar();
   */
  collapseSidebar(): void {
    const currentSize = this.screenSizeSubject$.value;

    if (!currentSize.isMobile) {
      this.updateSidebarState({
        expanded: false,
        collapsed: true,
        mobileOpen: false,
        currentWidth: this.config.sidebar.collapsedWidth
      });
      this.emitEvent('sidebar.collapse');
      this.saveUserPreferences();
    }
  }

  /**
   * Set sidebar expanded state
   *
   * @description
   * Directly set sidebar expanded state. Useful for initialization.
   *
   * @param expanded - Desired expanded state
   *
   * @example
   * this.layoutService.setSidebarExpanded(true);
   */
  setSidebarExpanded(expanded: boolean): void {
    const currentSize = this.screenSizeSubject$.value;

    if (!currentSize.isMobile) {
      if (expanded) {
        this.expandSidebar();
      } else {
        this.collapseSidebar();
      }
    }
  }

  // ========== PUBLIC METHODS - MOBILE SIDEBAR ==========

  /**
   * Open mobile sidebar overlay
   *
   * @description
   * Opens sidebar overlay on mobile devices.
   * No effect on desktop/tablet.
   *
   * @example
   * this.layoutService.openMobileSidebar();
   */
  openMobileSidebar(): void {
    const currentSize = this.screenSizeSubject$.value;

    if (currentSize.isMobile) {
      this.updateSidebarState({
        ...this.sidebarState$.value,
        mobileOpen: true
      });
      this.emitEvent('sidebar.mobile.open');
    }
  }

  /**
   * Close mobile sidebar overlay
   *
   * @description
   * Closes sidebar overlay on mobile devices.
   * Automatically closes after navigation.
   *
   * @example
   * this.layoutService.closeMobileSidebar();
   */
  closeMobileSidebar(): void {
    const currentSize = this.screenSizeSubject$.value;

    if (currentSize.isMobile) {
      this.updateSidebarState({
        ...this.sidebarState$.value,
        mobileOpen: false
      });
      this.emitEvent('sidebar.mobile.close');
    }
  }

  /**
   * Toggle mobile sidebar overlay
   *
   * @description
   * Toggles mobile sidebar overlay state.
   * Only affects mobile devices.
   *
   * @example
   * this.layoutService.toggleMobileSidebar();
   */
  toggleMobileSidebar(): void {
    const currentState = this.sidebarState$.value;

    if (currentState.mobileOpen) {
      this.closeMobileSidebar();
    } else {
      this.openMobileSidebar();
    }
  }

  // ========== PUBLIC METHODS - LAYOUT STATE ==========

  /**
   * Get complete layout state snapshot
   *
   * @description
   * Returns current state of entire layout system.
   * Useful for debugging and logging.
   *
   * @returns Complete layout state
   *
   * @example
   * const state = this.layoutService.getLayoutState();
   * console.log('Layout state:', state);
   */
  getLayoutState(): LayoutState {
    return {
      sidebar: this.sidebarState$.value,
      screenSize: this.screenSizeSubject$.value,
      timestamp: Date.now()
    };
  }

  /**
   * Get current sidebar state
   *
   * @description
   * Returns current sidebar state snapshot.
   *
   * @returns Current sidebar state
   *
   * @example
   * const sidebarState = this.layoutService.getSidebarState();
   */
  getSidebarState(): SidebarState {
    return this.sidebarState$.value;
  }

  /**
   * Get current screen size
   *
   * @description
   * Returns current screen size snapshot.
   *
   * @returns Current screen size
   *
   * @example
   * const screenSize = this.layoutService.getScreenSize();
   */
  getScreenSize(): ScreenSize {
    return this.screenSizeSubject$.value;
  }

  /**
   * Get user preferences
   *
   * @description
   * Returns current user preferences.
   *
   * @returns User preferences
   *
   * @example
   * const prefs = this.layoutService.getUserPreferences();
   */
  getUserPreferences(): UserPreferences {
    return this.userPreferencesSubject$.value;
  }

  /**
   * Update user preferences
   *
   * @description
   * Updates and persists user preferences.
   *
   * @param preferences - New user preferences
   *
   * @example
   * this.layoutService.updateUserPreferences({
   *   sidebarExpanded: true,
   *   theme: 'dark'
   * });
   */
  updateUserPreferences(preferences: Partial<UserPreferences>): void {
    const current = this.userPreferencesSubject$.value;
    const updated = { ...current, ...preferences };
    this.userPreferencesSubject$.next(updated);
    this.saveUserPreferences();
  }

  // ========== PRIVATE METHODS ==========

  /**
   * Update sidebar state
   *
   * @description
   * Internal method to update sidebar state.
   * Emits new state to all subscribers.
   *
   * @param state - New sidebar state
   * @private
   */
  private updateSidebarState(state: SidebarState): void {
    this.sidebarState$.next(state);
  }

  /**
   * Update screen size
   *
   * @description
   * Internal method to update screen size.
   * Automatically determines breakpoints.
   *
   * @param size - New screen size
   * @private
   */
  private updateScreenSize(size: ScreenSize): void {
    const previousSize = this.screenSizeSubject$.value;
    this.screenSizeSubject$.next(size);

    // Detect breakpoint changes
    const breakpointChanged =
      previousSize.isMobile !== size.isMobile ||
      previousSize.isTablet !== size.isTablet ||
      previousSize.isDesktop !== size.isDesktop;

    if (breakpointChanged) {
      this.emitEvent('breakpoint.change', {
        from: BreakpointUtils.getBreakpoint(previousSize.width),
        to: BreakpointUtils.getBreakpoint(size.width)
      });

      // Auto-adjust sidebar for breakpoint changes
      this.adjustSidebarForBreakpoint(size);
    }
  }

  /**
   * Adjust sidebar state for breakpoint changes
   *
   * @description
   * Automatically adjusts sidebar when breakpoint changes.
   * Ensures consistent behavior across devices.
   * Prevents concurrent resize operations.
   *
   * @param screenSize - New screen size
   * @private
   */
  private adjustSidebarForBreakpoint(screenSize: ScreenSize): void {
    // Prevent concurrent resize operations
    if (this.resizeInProgress) {
      return;
    }

    this.resizeInProgress = true;

    if (screenSize.isMobile) {
      // On mobile, ensure overlay is closed
      this.updateSidebarState({
        ...this.sidebarState$.value,
        mobileOpen: false,
        currentWidth: this.config.sidebar.mobileWidth
      });
    } else if (screenSize.isTablet) {
      // On tablet, use user preference or default to expanded
      const userPref = this.userPreferencesSubject$.value;
      const expanded = userPref.sidebarExpanded;
      this.updateSidebarState({
        expanded,
        collapsed: !expanded,
        mobileOpen: false,
        currentWidth: expanded ? this.config.breakpoints.tablet.sidebarWidth : this.config.sidebar.collapsedWidth
      });
    } else {
      // On desktop, use user preference
      const userPref = this.userPreferencesSubject$.value;
      const expanded = userPref.sidebarExpanded;
      this.updateSidebarState({
        expanded,
        collapsed: !expanded,
        mobileOpen: false,
        currentWidth: expanded ? this.config.sidebar.expandedWidth : this.config.sidebar.collapsedWidth
      });
    }

    // Reset flag after short delay to prevent rapid resize issues
    setTimeout(() => {
      this.resizeInProgress = false;
    }, 200);
  }

  /**
   * Initialize window size
   *
   * @description
   * Captures initial window size on service initialization.
   *
   * @private
   */
  private initializeWindowSize(): void {
    if (typeof window !== 'undefined') {
      const width = window.innerWidth;
      const height = window.innerHeight;

      this.updateScreenSize({
        isMobile: BreakpointUtils.isMobile(width),
        isTablet: BreakpointUtils.isTablet(width),
        isDesktop: BreakpointUtils.isDesktop(width),
        width,
        height
      });
    }
  }

  /**
   * Setup window resize listener
   *
   * @description
   * Listens for window resize events with debouncing.
   * Updates screen size state on resize.
   *
   * @private
   */
  private setupWindowResizeListener(): void {
    if (typeof window !== 'undefined') {
      fromEvent(window, 'resize')
        .pipe(
          debounceTime(150),
          takeUntil(this.destroy$)
        )
        .subscribe(() => {
          const width = window.innerWidth;
          const height = window.innerHeight;

          this.updateScreenSize({
            isMobile: BreakpointUtils.isMobile(width),
            isTablet: BreakpointUtils.isTablet(width),
            isDesktop: BreakpointUtils.isDesktop(width),
            width,
            height
          });

          this.emitEvent('resize', { width, height });
        });
    }
  }

  /**
   * Emit layout event
   *
   * @description
   * Emits a layout event to all subscribers.
   *
   * @param type - Event type
   * @param data - Additional event data
   * @private
   */
  private emitEvent(type: LayoutEventType, data?: Record<string, any>): void {
    this.eventEmitter.next({
      type,
      timestamp: Date.now(),
      data
    });
  }

  /**
   * Validate state after initialization
   *
   * @description
   * Ensures layout state is consistent after service initialization.
   * Fixes any inconsistencies that might occur from localStorage corruption
   * or page refresh during state changes.
   *
   * @private
   */
  private validateStateAfterInit(): void {
    const currentSize = this.screenSizeSubject$.value;
    const currentSidebar = this.sidebarState$.value;

    // Ensure mobile sidebar is closed on initialization
    if (currentSize.isMobile && currentSidebar.mobileOpen) {
      this.closeMobileSidebar();
    }

    // Ensure sidebar state matches current breakpoint
    if (currentSize.isMobile && currentSidebar.expanded) {
      this.updateSidebarState({
        ...currentSidebar,
        expanded: false,
        collapsed: false,
        mobileOpen: false,
        currentWidth: this.config.sidebar.mobileWidth
      });
    }

    // Validate localStorage data integrity
    try {
      const stored = window.localStorage.getItem('layout-preferences');
      if (stored) {
        const prefs = JSON.parse(stored);
        // Ensure sidebarExpanded is boolean
        if (typeof prefs.sidebarExpanded !== 'boolean') {
          console.warn('Invalid sidebarExpanded in localStorage, resetting');
          this.saveUserPreferences(); // Reset to defaults
        }
      }
    } catch (error) {
      // Clear corrupted localStorage data
      console.warn('localStorage corrupted, clearing preferences');
      try {
        window.localStorage.removeItem('layout-preferences');
      } catch (e) {
        // Ignore localStorage errors
      }
    }
  }

  /**
   * Load user preferences from localStorage
   *
   * @description
   * Loads user preferences from browser localStorage.
   * Uses defaults if no preferences are stored or if loading fails.
   * Handles corrupted data gracefully.
   *
   * @private
   */
  private loadUserPreferences(): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        const stored = window.localStorage.getItem('layout-preferences');
        if (stored) {
          const preferences = JSON.parse(stored) as UserPreferences;
          // Validate loaded preferences before applying
          if (preferences && typeof preferences === 'object' && 'sidebarExpanded' in preferences) {
            this.userPreferencesSubject$.next(preferences);
          } else {
            // Use defaults if data is corrupted
            console.warn('Invalid preferences format, using defaults');
          }
        }
      } catch (error) {
        console.warn('Failed to load user preferences:', error);
        // Use defaults if loading fails
      }
    }
  }

  /**
   * Save user preferences to localStorage
   *
   * @description
   * Persists user preferences to browser localStorage.
   *
   * @private
   */
  private saveUserPreferences(): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        const preferences = {
          ...this.userPreferencesSubject$.value,
          sidebarExpanded: this.sidebarState$.value.expanded
        };
        window.localStorage.setItem('layout-preferences', JSON.stringify(preferences));
      } catch (error) {
        console.warn('Failed to save user preferences:', error);
      }
    }
  }

  // ========== LIFECYCLE HOOKS ==========

  /**
   * Cleanup on service destruction
   *
   * @description
   * Implements OnDestroy for proper cleanup.
   * Prevents memory leaks by completing all subjects.
   *
   * @example
   * // Automatically called by Angular when service is destroyed
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.eventEmitter.complete();
    this.sidebarState$.complete();
    this.screenSizeSubject$.complete();
    this.userPreferencesSubject$.complete();
  }
}