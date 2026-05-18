import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-search-box',
  templateUrl: './app-search-box.component.html',
  styleUrls: ['./app-search-box.component.css']
})
export class AppSearchBoxComponent {

  @Output() search = new EventEmitter<string>();

  onSearch(value: string): void {
    this.search.emit(value);
  }

}