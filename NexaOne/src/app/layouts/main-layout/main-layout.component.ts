import { Component, HostListener, OnInit } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router } from '@angular/router';
import { LoadingService } from '../../shared/services/loading.service';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit {

  isSidebarMobileOpen = false;
  isMobileView = false;
  isSidebarCollapsed = false;

  constructor(private router: Router, private loadingService: LoadingService) {}

  ngOnInit(): void {
    this.updateViewportState();
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.loadingService.show();
      }

      if (event instanceof NavigationEnd || event instanceof NavigationCancel || event instanceof NavigationError) {
        this.loadingService.hide();
      }
    });
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    this.updateViewportState();
  }

  toggleSidebar(): void {
    if (this.isMobileView) {
      this.isSidebarMobileOpen = !this.isSidebarMobileOpen;
      return;
    }

    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  closeSidebar(): void {
    this.isSidebarMobileOpen = false;
  }

  private updateViewportState(): void {
    this.isMobileView = window.innerWidth < 992;

    if (!this.isMobileView) {
      this.isSidebarMobileOpen = false;
    }
  }

}
