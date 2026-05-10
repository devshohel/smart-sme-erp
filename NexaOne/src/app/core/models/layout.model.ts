/**
 * LAYOUT MODEL - Type Definitions for Layout Management System
 *
 * @description
 * Centralized type definitions for the entire layout system.
 * Provides strongly-typed interfaces for sidebar state, screen size tracking,
 * and responsive configuration throughout the application.
 *
 * @architecture
 * These models are used by:
 * - LayoutService (state management)
 * - Layout components (sidebar, navbar, main-layout)
 * - Feature components (accessing layout state)
 *
 * @usage
 * import { SidebarState, ScreenSize } from '@core/models/layout.model';
 *
 * @version 1.0.0
 * @since 2026-05-10
 */

/**
 * Sidebar state interface
 *
 * @description
 * Represents the complete state of the sidebar component across all devices.
 * Supports desktop expanded/collapsed states and mobile overlay state.
 *
 * @property expanded - Desktop: sidebar is fully expanded (280px)
 * @property collapsed - Desktop: sidebar is collapsed to icons only (70px)
 * @property mobileOpen - Mobile: sidebar overlay is currently visible
 * @property currentWidth - Current sidebar width in pixels (calculated)
 *
 * @example
 * const defaultState: SidebarState = {
 *   expanded: true,
 *   collapsed: false,
 *   mobileOpen: false,
 *   currentWidth: 280
 * };
 */
export interface SidebarState {
  /** Desktop sidebar is fully expanded */
  expanded: boolean;

  /** Desktop sidebar is collapsed to icons only */
  collapsed: boolean;

  /** Mobile sidebar overlay is currently open */
  mobileOpen: boolean;

  /** Current sidebar width in pixels (calculated from state) */
  currentWidth: number;
}

/**
 * Screen size interface
 *
 * @description
 * Represents the current viewport dimensions and breakpoint classification.
 * Updated on window resize events with debouncing for performance.
 *
 * @property isMobile - Mobile breakpoint (< 768px)
 * @property isTablet - Tablet breakpoint (768px - 991px)
 * @property isDesktop - Desktop breakpoint (>= 992px)
 * @property width - Current window width in pixels
 * @property height - Current window height in pixels
 *
 * @example
 * const screenSize: ScreenSize = {
 *   isMobile: false,
 *   isTablet: false,
 *   isDesktop: true,
 *   width: 1920,
 *   height: 1080
 * };
 */
export interface ScreenSize {
  /** Device matches mobile breakpoint */
  isMobile: boolean;

  /** Device matches tablet breakpoint */
  isTablet: boolean;

  /** Device matches desktop breakpoint */
  isDesktop: boolean;

  /** Current window width in pixels */
  width: number;

  /** Current window height in pixels */
  height: number;
}

/**
 * Breakpoint configuration interface
 *
 * @description
 * Defines a single responsive breakpoint with configuration for
 * sidebar width and behavior at that breakpoint.
 *
 * @property name - Breakpoint identifier (mobile, tablet, desktop)
 * @property minWidth - Minimum width in pixels for this breakpoint
 * @property maxWidth - Maximum width in pixels (undefined for desktop)
 * @property sidebarWidth - Sidebar width in pixels for this breakpoint
 *
 * @example
 * const mobileBreakpoint: Breakpoint = {
 *   name: 'mobile',
 *   minWidth: 0,
 *   maxWidth: 767,
 *   sidebarWidth: 280
 * };
 */
export interface Breakpoint {
  /** Breakpoint identifier */
  name: string;

  /** Minimum width in pixels */
  minWidth: number;

  /** Maximum width in pixels (undefined for largest breakpoint) */
  maxWidth?: number;

  /** Sidebar width in pixels for this breakpoint */
  sidebarWidth: number;
}

/**
 * Layout configuration interface
 *
 * @description
 * Complete configuration for the entire layout system including
 * breakpoints, sidebar behavior, and navbar settings.
 *
 * @property breakpoints - Responsive breakpoint definitions
 * @property sidebar - Sidebar behavior and dimensions
 * @property navbar - Navbar configuration
 * @property animations - Animation duration settings
 *
 * @example
 * const config: LayoutConfig = {
 *   breakpoints: { ... },
 *   sidebar: { ... },
 *   navbar: { ... },
 *   animations: { ... }
 * };
 */
export interface LayoutConfig {
  /** Responsive breakpoint definitions */
  breakpoints: {
    /** Mobile breakpoint configuration */
    mobile: Breakpoint;

    /** Tablet breakpoint configuration */
    tablet: Breakpoint;

    /** Desktop breakpoint configuration */
    desktop: Breakpoint;
  };

  /** Sidebar behavior and dimensions */
  sidebar: {
    /** Width when fully expanded (desktop) */
    expandedWidth: number;

    /** Width when collapsed to icons (desktop) */
    collapsedWidth: number;

    /** Width on mobile devices */
    mobileWidth: number;

    /** Animation duration in milliseconds */
    transitionDuration: number;

    /** Enable/disable sidebar animations */
    enableAnimations: boolean;
  };

  /** Navbar configuration */
  navbar: {
    /** Navbar height in pixels */
    height: number;

    /** Should navbar be sticky at top */
    sticky: boolean;

    /** Show/hide navbar on scroll */
    autoHide: boolean;
  };

  /** Animation duration settings */
  animations: {
    /** Fast animation duration (ms) */
    fast: number;

    /** Normal animation duration (ms) */
    normal: number;

    /** Slow animation duration (ms) */
    slow: number;
  };
}

/**
 * Layout state interface
 *
 * @description
 * Complete state snapshot of the entire layout system.
 * Combines sidebar state and screen size for comprehensive layout tracking.
 *
 * @property sidebar - Current sidebar state
 * @property screenSize - Current screen size information
 * @property timestamp - When this state was captured
 *
 * @example
 * const layoutState: LayoutState = {
 *   sidebar: { ... },
 *   screenSize: { ... },
 *   timestamp: Date.now()
 * };
 */
export interface LayoutState {
  /** Current sidebar state */
  sidebar: SidebarState;

  /** Current screen size information */
  screenSize: ScreenSize;

  /** State capture timestamp */
  timestamp: number;
}

/**
 * Menu item interface (for future sidebar menu configuration)
 *
 * @description
 * Structure for sidebar menu items to support dynamic menu generation.
 * Enables nested menus, permissions, and external links.
 *
 * @property id - Unique identifier for the menu item
 * @property label - Display text for the menu item
 * @property icon - Bootstrap icon class name
 * @property route - Angular route path (empty for external links)
 * @property external - External URL (opens in new tab)
 * @property permission - Required permission to view this item
 * @property children - Nested menu items (for dropdowns)
 * @property enabled - Whether this menu item is active
 *
 * @example
 * const menuItem: MenuItem = {
 *   id: 'dashboard',
 *   label: 'Dashboard',
 *   icon: 'bi-grid-1x2-fill',
 *   route: '/dashboard',
 *   permission: 'DASHBOARD_VIEW',
 *   children: [],
 *   enabled: true
 * };
 */
export interface MenuItem {
  /** Unique identifier */
  id: string;

  /** Display text */
  label: string;

  /** Bootstrap icon class */
  icon: string;

  /** Angular route path */
  route?: string;

  /** External URL (opens in new tab) */
  external?: string;

  /** Required permission */
  permission?: string;

  /** Nested menu items */
  children?: MenuItem[];

  /** Is this menu item enabled */
  enabled: boolean;
}

/**
 * Layout event types
 *
 * @description
 * Type definitions for events emitted by the layout system.
 * Components can subscribe to these events for custom behavior.
 *
 * @property 'sidebar.toggle' - Sidebar was toggled
 * @property 'sidebar.expand' - Sidebar was expanded
 * @property 'sidebar.collapse' - Sidebar was collapsed
 * @property 'sidebar.mobile.open' - Mobile sidebar was opened
 * @property 'sidebar.mobile.close' - Mobile sidebar was closed
 * @property 'breakpoint.change' - Screen breakpoint changed
 * @property 'resize' - Window was resized
 */
export type LayoutEventType =
  | 'sidebar.toggle'
  | 'sidebar.expand'
  | 'sidebar.collapse'
  | 'sidebar.mobile.open'
  | 'sidebar.mobile.close'
  | 'breakpoint.change'
  | 'resize';

/**
 * Layout event interface
 *
 * @description
 * Structure for events emitted by the layout system.
 * Provides context about what changed and when.
 *
 * @property type - Event type identifier
 * @property timestamp - When the event occurred
 * @property data - Additional event data
 *
 * @example
 * const event: LayoutEvent = {
 *   type: 'sidebar.toggle',
 *   timestamp: Date.now(),
 *   data: { from: 'expanded', to: 'collapsed' }
 * };
 */
export interface LayoutEvent {
  /** Event type identifier */
  type: LayoutEventType;

  /** Event timestamp */
  timestamp: number;

  /** Additional event data */
  data?: Record<string, any>;
}

/**
 * User preferences interface (for future feature)
 *
 * @description
 * User-specific layout preferences that can be persisted
 * to localStorage or backend for personalized experience.
 *
 * @property sidebarExpanded - User's preferred sidebar state
 * @property theme - Preferred theme (light/dark)
 * @property language - Preferred language
 * @property density - UI density (compact/comfortable)
 */
export interface UserPreferences {
  /** User's preferred sidebar state */
  sidebarExpanded: boolean;

  /** Preferred theme */
  theme: 'light' | 'dark';

  /** Preferred language */
  language: string;

  /** UI density preference */
  density: 'compact' | 'comfortable';
}