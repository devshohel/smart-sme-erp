import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { CustomersRoutingModule } from './customers-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { CustomerAgingComponent } from './customer-aging/customer-aging.component';
import { CustomerDetailsComponent } from './customer-details/customer-details.component';
import { CustomerFormComponent } from './customer-form/customer-form.component';
import { CustomerListComponent } from './customer-list/customer-list.component';
import { CustomerReceiptDetailsComponent } from './customer-receipts/customer-receipt-details/customer-receipt-details.component';
import { CustomerReceiptFormComponent } from './customer-receipts/customer-receipt-form/customer-receipt-form.component';
import { CustomerReceiptListComponent } from './customer-receipts/customer-receipt-list/customer-receipt-list.component';


@NgModule({
  declarations: [
    CustomerListComponent,
    CustomerAgingComponent,
    CustomerFormComponent,
    CustomerDetailsComponent,
    CustomerReceiptListComponent,
    CustomerReceiptFormComponent,
    CustomerReceiptDetailsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    CustomersRoutingModule,
    SharedModule
  ]
})
export class CustomersModule { }
