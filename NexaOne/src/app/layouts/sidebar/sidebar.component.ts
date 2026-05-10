// src/app/layouts/sidebar/sidebar.component.ts

import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  
  isInventoryOpen: boolean = false; 

  constructor() { }

  ngOnInit(): void { }

  // ড্রপডাউন টগল করার ফাংশন
  toggleInventory() {
    this.isInventoryOpen = !this.isInventoryOpen;
  }
}