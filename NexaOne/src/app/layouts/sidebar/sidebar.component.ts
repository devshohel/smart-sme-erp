import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  isProductsOpen: boolean = false;
  isInventoryOpen: boolean = false;

  constructor() { }

  ngOnInit(): void { }

  toggleProducts() {
    this.isProductsOpen = !this.isProductsOpen;
  }

  toggleInventory() {
    this.isInventoryOpen = !this.isInventoryOpen;
  }
}
