import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  templateUrl: './app-page-header.component.html',
  styleUrls: ['./app-page-header.component.css']
})
export class AppPageHeaderComponent {

  @Input() title: string = '';
  @Input() subtitle: string = '';

}