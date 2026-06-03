import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SalesRoutingModule } from './sales-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { SalesStatusBadgeComponent } from './components/sales-status-badge/sales-status-badge.component';
import { OrdersComponent } from './orders/orders.component';
import { InvoicesComponent } from './invoices/invoices.component';
import { ReturnsComponent } from './returns/returns.component';


@NgModule({
  declarations: [
    SalesStatusBadgeComponent,
    OrdersComponent,
    InvoicesComponent,
    ReturnsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SalesRoutingModule,
    SharedModule
  ]
})
export class SalesModule { }
