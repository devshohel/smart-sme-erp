/**
 * RESPONSIVE CONFIGURATION - Breakpoint and Layout Settings
 *
 * @description
 * Centralized configuration for responsive breakpoints, sidebar dimensions,
 * and animation durations. Single source of truth for all layout-related
 * constants throughout the application.
 *
 * @architecture
 * This configuration is:
 * - Imported by LayoutService for state management
 * - Used by components for consistent behavior
 * - Reference for responsive design decisions
 *
 * @usage
 * import { BREAKPOINTS, SIDEBAR_WIDTHS, ANIMATION_DURATION } from '@core/config/responsive.config';
 *
 * @version 1.0.0
 * @since 2026-05-10
 */

/**
 * Responsive breakpoint definitions
 *
 * @description
 * Standard breakpoints for responsive design. These align with
 * Bootstrap 5 breakpoints for consistency with the UI framework.
 *
 * @constant
 * @type {Object}
 *
 * @property MOBILE - Mobile devices (< 768px)
 * @property TABLET - Tablet devices (768px - 991px)
 * @property DESKTOP - Desktop devices (>= 992px)
 * @property WIDE - Wide desktop screens (>= 1200px)
 * @property ULTRA_WIDE - Ultra-wide screens (>= 1440px)
 *
 * @example
 * if (window.innerWidth < BREAKPOINTS.MOBILE) {
 *   // Apply mobile-specific logic
 * }
 */
export const BREAKPOINTS = {
  /** Mobile breakpoint: 0px to 767px */
  MOBILE: 768,

  /** Tablet breakpoint: 768px to 991px */
  TABLET: 992,

  /** Desktop breakpoint: 992px and above */
  DESKTOP: 992,

  /** Wide desktop breakpoint: 1200px and above */
  WIDE: 1200,

  /** Ultra-wide breakpoint: 1440px and above */
  ULTRA_WIDE: 1440
} as const;

/**
 * Sidebar width configurations
 *
 * @description
 * Sidebar dimensions for different states and breakpoints.
 * Ensures consistent sidebar behavior across the application.
 *
 * @constant
 * @type {Object}
 *
 * @property EXPANDED - Full sidebar width on desktop (280px)
 * @property COLLAPSED - Collapsed sidebar width, icons only (70px)
 * @property MOBILE - Full sidebar width on mobile (280px)
 * @property TABLET - Full sidebar width on tablet (240px)
 *
 * @example
 * const sidebarWidth = isCollapsed ? SIDEBAR_WIDTHS.COLLAPSED : SIDEBAR_WIDTHS.EXPANDED;
 */
export const SIDEBAR_WIDTHS = {
  /** Desktop expanded sidebar width: 280px */
  EXPANDED: 280,

  /** Desktop collapsed sidebar width: 70px */
  COLLAPSED: 70,

  /** Mobile sidebar width: 280px */
  MOBILE: 280,

  /** Tablet sidebar width: 240px */
  TABLET: 240
} as const;

/**
 * Animation duration settings
 *
 * @description
 * Standard animation durations for consistent animations
 * throughout the layout system. Times are in milliseconds.
 *
 * @constant
 * @type {Object}
 *
 * @property FAST - Quick animations (150ms)
 * @property NORMAL - Standard animations (300ms)
 * @property SLOW - Deliberate animations (500ms)
 *
 * @example
 * transition: `width ${ANIMATION_DURATION.NORMAL}ms ease`
 */
export const ANIMATION_DURATION = {
  /** Fast animation: 150ms */
  FAST: 150,

  /** Normal animation: 300ms */
  NORMAL: 300,

  /** Slow animation: 500ms */
  SLOW: 500
} as const;

/**
 * Layout configuration object
 *
 * @description
 * Complete layout configuration conforming to the LayoutConfig interface.
 * This is the main configuration object imported by LayoutService.
 *
 * @constant
 * @type {LayoutConfig}
 *
 * @example
 * import { LAYOUT_CONFIG } from '@core/config/responsive.config';
 *
 * const config = LAYOUT_CONFIG;
 * const sidebarWidth = config.sidebar.expandedWidth;
 */
export const LAYOUT_CONFIG = {
  /** Responsive breakpoint definitions */
  breakpoints: {
    /** Mobile breakpoint configuration */
    mobile: {
      name: 'mobile',
      minWidth: 0,
      maxWidth: 767,
      sidebarWidth: SIDEBAR_WIDTHS.MOBILE
    },

    /** Tablet breakpoint configuration */
    tablet: {
      name: 'tablet',
      minWidth: BREAKPOINTS.MOBILE,
      maxWidth: 991,
      sidebarWidth: SIDEBAR_WIDTHS.TABLET
    },

    /** Desktop breakpoint configuration */
    desktop: {
      name: 'desktop',
      minWidth: BREAKPOINTS.DESKTOP,
      sidebarWidth: SIDEBAR_WIDTHS.EXPANDED
    }
  },

  /** Sidebar behavior and dimensions */
  sidebar: {
    /** Width when fully expanded on desktop */
    expandedWidth: SIDEBAR_WIDTHS.EXPANDED,

    /** Width when collapsed to icons on desktop */
    collapsedWidth: SIDEBAR_WIDTHS.COLLAPSED,

    /** Width on mobile devices */
    mobileWidth: SIDEBAR_WIDTHS.MOBILE,

    /** Sidebar transition duration in milliseconds */
    transitionDuration: ANIMATION_DURATION.NORMAL,

    /** Enable smooth sidebar animations */
    enableAnimations: true
  },

  /** Navbar configuration */
  navbar: {
    /** Fixed navbar height in pixels */
    height: 64,

    /** Navbar should stick to top of viewport */
    sticky: true,

    /** Auto-hide navbar on scroll down (future feature) */
    autoHide: false
  },

  /** Animation duration settings */
  animations: {
    /** Fast animation duration */
    fast: ANIMATION_DURATION.FAST,

    /** Normal animation duration */
    normal: ANIMATION_DURATION.NORMAL,

    /** Slow animation duration */
    slow: ANIMATION_DURATION.SLOW
  }
} as const;

/**
 * Media query templates
 *
 * @description
 * Pre-defined media query templates for responsive styles.
 * Can be used in components or with Angular CDK BreakpointObserver.
 *
 * @constant
 * @type {Object}
 *
 * @example
 * import { MEDIA_QUERIES } from '@core/config/responsive.config';
 *
 * const isMobile = window.matchMedia(MEDIA_QUERIES.MOBILE).matches;
 */
export const MEDIA_QUERIES = {
  /** Mobile devices only */
  MOBILE: `(max-width: ${BREAKPOINTS.MOBILE - 1}px)`,

  /** Tablet devices only */
  TABLET: `(min-width: ${BREAKPOINTS.MOBILE}px) and (max-width: ${BREAKPOINTS.TABLET - 1}px)`,

  /** Desktop devices and above */
  DESKTOP: `(min-width: ${BREAKPOINTS.DESKTOP}px)`,

  /** Tablet and below */
  TABLET_AND_BELOW: `(max-width: ${BREAKPOINTS.TABLET - 1}px)`,

  /** Desktop and above */
  DESKTOP_AND_UP: `(min-width: ${BREAKPOINTS.DESKTOP}px)`,

  /** Mobile and tablet */
  MOBILE_AND_TABLET: `(max-width: ${BREAKPOINTS.TABLET - 1}px)`,

  /** Wide desktop screens */
  WIDE: `(min-width: ${BREAKPOINTS.WIDE}px)`,

  /** Ultra-wide screens */
  ULTRA_WIDE: `(min-width: ${BREAKPOINTS.ULTRA_WIDE}px)`

} as const;

/**
 * Z-index layer configuration
 *
 * @description
 * Z-index values for layout elements to ensure proper layering.
 * Following a systematic approach for z-index management.
 *
 * @constant
 * @type {Object}
 *
 * @property DROPDOWN - Dropdown menus and popups
 * @property STICKY_HEADER - Sticky navbar
 * @property FIXED_SIDEBAR - Fixed sidebar
 * @property MODAL_BACKDROP - Modal overlay backdrop
 * @property MODAL - Modal dialog
 * @property POPOVER - Popovers and tooltips
 * @property TOP_LAYER - Highest priority layer
 *
 * @example
 * .sidebar { z-index: Z_INDEX.FIXED_SIDEBAR; }
 */
export const Z_INDEX = {
  /** Base layer */
  BASE: 1,

  /** Dropdown menus */
  DROPDOWN: 1000,

  /** Sticky header */
  STICKY_HEADER: 1020,

  /** Fixed sidebar */
  FIXED_SIDEBAR: 1030,

  /** Modal backdrop */
  MODAL_BACKDROP: 1040,

  /** Modal dialog */
  MODAL: 1050,

  /** Popovers and tooltips */
  POPOVER: 1060,

  /** Top layer for critical elements */
  TOP_LAYER: 1070,

  /** Mobile sidebar overlay */
  MOBILE_OVERLAY: 999
} as const;

/**
 * Breakpoint detector utilities
 *
 * @description
 * Utility functions for breakpoint detection and comparison.
 *
 * @example
 * import { BreakpointUtils } from '@core/config/responsive.config';
 *
 * if (BreakpointUtils.isMobile(767)) {
 *   // Handle mobile view
 * }
 */
export class BreakpointUtils {
  /**
   * Check if width is in mobile range
   *
   * @param width - Window width in pixels
   * @returns true if mobile breakpoint
   */
  static isMobile(width: number): boolean {
    return width < BREAKPOINTS.MOBILE;
  }

  /**
   * Check if width is in tablet range
   *
   * @param width - Window width in pixels
   * @returns true if tablet breakpoint
   */
  static isTablet(width: number): boolean {
    return width >= BREAKPOINTS.MOBILE && width < BREAKPOINTS.TABLET;
  }

  /**
   * Check if width is in desktop range
   *
   * @param width - Window width in pixels
   * @returns true if desktop breakpoint
   */
  static isDesktop(width: number): boolean {
    return width >= BREAKPOINTS.DESKTOP;
  }

  /**
   * Get current breakpoint name
   *
   * @param width - Window width in pixels
   * @returns breakpoint name
   */
  static getBreakpoint(width: number): 'mobile' | 'tablet' | 'desktop' {
    if (this.isMobile(width)) return 'mobile';
    if (this.isTablet(width)) return 'tablet';
    return 'desktop';
  }

  /**
   * Get sidebar width for breakpoint
   *
   * @param width - Window width in pixels
   * @returns sidebar width in pixels
   */
  static getSidebarWidth(width: number): number {
    const breakpoint = this.getBreakpoint(width);
    switch (breakpoint) {
      case 'mobile':
        return SIDEBAR_WIDTHS.MOBILE;
      case 'tablet':
        return SIDEBAR_WIDTHS.TABLET;
      case 'desktop':
        return SIDEBAR_WIDTHS.EXPANDED;
      default:
        return SIDEBAR_WIDTHS.EXPANDED;
    }
  }
}

/**
 * Configuration validator
 *
 * @description
 * Validates configuration values and provides error messages
 * for invalid settings.
 *
 * @example
 * import { ConfigValidator } from '@core/config/responsive.config';
 *
 * if (!ConfigValidator.isValid()) {
 *   console.error('Invalid configuration');
 * }
 */
export class ConfigValidator {
  /**
   * Validate configuration values
   *
   * @returns true if configuration is valid
   */
  static isValid(): boolean {
    return (
      BREAKPOINTS.MOBILE > 0 &&
      BREAKPOINTS.TABLET > BREAKPOINTS.MOBILE &&
      BREAKPOINTS.DESKTOP >= BREAKPOINTS.TABLET &&
      SIDEBAR_WIDTHS.EXPANDED > SIDEBAR_WIDTHS.COLLAPSED &&
      ANIMATION_DURATION.FAST > 0 &&
      ANIMATION_DURATION.NORMAL > ANIMATION_DURATION.FAST &&
      ANIMATION_DURATION.SLOW > ANIMATION_DURATION.NORMAL
    );
  }

  /**
   * Get validation errors
   *
   * @returns array of validation error messages
   */
  static getValidationErrors(): string[] {
    const errors: string[] = [];

    if (BREAKPOINTS.MOBILE <= 0) {
      errors.push('BREAKPOINTS.MOBILE must be greater than 0');
    }

    if (BREAKPOINTS.TABLET <= BREAKPOINTS.MOBILE) {
      errors.push('BREAKPOINTS.TABLET must be greater than BREAKPOINTS.MOBILE');
    }

    if (BREAKPOINTS.DESKTOP < BREAKPOINTS.TABLET) {
      errors.push('BREAKPOINTS.DESKTOP must be greater than or equal to BREAKPOINTS.TABLET');
    }

    if (SIDEBAR_WIDTHS.EXPANDED <= SIDEBAR_WIDTHS.COLLAPSED) {
      errors.push('SIDEBAR_WIDTHS.EXPANDED must be greater than SIDEBAR_WIDTHS.COLLAPSED');
    }

    if (ANIMATION_DURATION.FAST <= 0) {
      errors.push('ANIMATION_DURATION.FAST must be greater than 0');
    }

    if (ANIMATION_DURATION.NORMAL <= ANIMATION_DURATION.FAST) {
      errors.push('ANIMATION_DURATION.NORMAL must be greater than ANIMATION_DURATION.FAST');
    }

    if (ANIMATION_DURATION.SLOW <= ANIMATION_DURATION.NORMAL) {
      errors.push('ANIMATION_DURATION.SLOW must be greater than ANIMATION_DURATION.NORMAL');
    }

    return errors;
  }
}