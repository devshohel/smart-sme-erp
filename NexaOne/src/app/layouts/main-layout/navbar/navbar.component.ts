import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {

  @Output()
  sidebarToggle = new EventEmitter<void>();

  toggleSidebar(): void {
    this.sidebarToggle.emit();
  }

}
