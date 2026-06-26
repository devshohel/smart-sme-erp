import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AppCardComponent } from './components/app-card/app-card.component';
import { AppTableComponent } from './components/app-table/app-table.component';
import { AppPageHeaderComponent } from './components/app-page-header/app-page-header.component';
import { AppModalComponent } from './components/app-modal/app-modal.component';
import { AppSearchBoxComponent } from './components/app-search-box/app-search-box.component';
import { AppBreadcrumbComponent } from './components/app-breadcrumb/app-breadcrumb.component';
import { AppEmptyStateComponent } from './components/app-empty-state/app-empty-state.component';
import { GlobalLoadingComponent } from './components/global-loading/global-loading.component';
import { NotificationContainerComponent } from './components/notification-container/notification-container.component';

@NgModule({
  declarations: [
    AppCardComponent,
    AppTableComponent,
    AppPageHeaderComponent,
    AppModalComponent,
    AppSearchBoxComponent,
    AppBreadcrumbComponent,
    AppEmptyStateComponent,
    GlobalLoadingComponent,
    NotificationContainerComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ],
  exports: [
    AppCardComponent,
    AppTableComponent,
    AppPageHeaderComponent,
    AppModalComponent,
    AppSearchBoxComponent,
    AppBreadcrumbComponent,
    AppEmptyStateComponent,
    GlobalLoadingComponent,
    NotificationContainerComponent,
    FormsModule,
    CommonModule
  ]
})
export class SharedModule { }
