import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-table',
  templateUrl: './app-table.component.html',
  styleUrls: ['./app-table.component.css']
})
export class AppTableComponent {

  @Input() headers: string[] = [];
  @Input() loading = false;
  @Input() empty = false;
  @Input() emptyTitle = 'No records found';
  @Input() emptyMessage = '';

}
