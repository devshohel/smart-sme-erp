import { Component, HostListener, OnInit } from '@angular/core';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit {

  isSidebarMobileOpen = false;
  isMobileView = false;

  ngOnInit(): void {
    this.updateViewportState();
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    this.updateViewportState();
  }

  toggleSidebar(): void {
    if (this.isMobileView) {
      this.isSidebarMobileOpen = !this.isSidebarMobileOpen;
    }
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
