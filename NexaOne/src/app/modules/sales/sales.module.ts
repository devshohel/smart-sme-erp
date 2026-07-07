import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SalesRoutingModule } from './sales-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { SalesStatusBadgeComponent } from './components/sales-status-badge/sales-status-badge.component';
import { OrdersComponent } from './orders/orders.component';
import { ReturnsComponent } from './returns/returns.component';
import { InvoiceListComponent } from './invoice-list/invoice-list.component';
import { InvoiceFormComponent } from './invoice-form/invoice-form.component';
import { PosComponent } from './pos/pos.component';
import { SaleItemsComponent } from './sale-items/sale-items.component';


@NgModule({
  declarations: [
    SalesStatusBadgeComponent,
    OrdersComponent,
    ReturnsComponent,
    InvoiceListComponent,
    InvoiceFormComponent,
    PosComponent,
    SaleItemsComponent
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
