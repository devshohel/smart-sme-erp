import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  templateUrl: './app-empty-state.component.html',
  styleUrls: ['./app-empty-state.component.css']
})
export class AppEmptyStateComponent {
  @Input() icon = 'bi-inbox';
  @Input() title = 'No records found';
  @Input() message = '';
}
