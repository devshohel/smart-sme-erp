import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AppCardComponent } from './components/app-card/app-card.component';
import { AppTableComponent } from './components/app-table/app-table.component';
import { AppPageHeaderComponent } from './components/app-page-header/app-page-header.component';
import { AppModalComponent } from './components/app-modal/app-modal.component';
import { AppSearchBoxComponent } from './components/app-search-box/app-search-box.component';

@NgModule({
  declarations: [
    AppCardComponent,
    AppTableComponent,
    AppPageHeaderComponent,
    AppModalComponent,
    AppSearchBoxComponent
  ],
  imports: [
    CommonModule,
    FormsModule
  ],
  exports: [
    AppCardComponent,
    AppTableComponent,
    AppPageHeaderComponent,
    AppModalComponent,
    AppSearchBoxComponent,
    FormsModule,
    CommonModule
  ]
})
export class SharedModule { }