import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() isMobileView = false;
  @Input() isMobileOpen = false;
  @Output() closeRequested = new EventEmitter<void>();

  isProductsOpen = true;
  isInventoryOpen = true;
  isSalesOpen = true;
  isCustomersOpen = true;
  isSuppliersOpen = true;
  isPurchasesOpen = true;

  constructor() {
    this.checkScreenSize();
  }

  @HostListener('window:resize')
  checkScreenSize(): void {
    this.isMobileView = window.innerWidth < 992;
  }

  toggleProducts(): void {
    this.isProductsOpen = !this.isProductsOpen;
  }

  toggleInventory(): void {
    this.isInventoryOpen = !this.isInventoryOpen;
  }

  toggleSales(): void {
    this.isSalesOpen = !this.isSalesOpen;
  }

  toggleCustomers(): void {
    this.isCustomersOpen = !this.isCustomersOpen;
  }

  toggleSuppliers(): void {
    this.isSuppliersOpen = !this.isSuppliersOpen;
  }

  togglePurchases(): void {
    this.isPurchasesOpen = !this.isPurchasesOpen;
  }

  closeSidebar(): void {
    if (this.isMobileView) {
      this.closeRequested.emit();
    }
  }
}
