import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';
import { SuppliersRoutingModule } from './suppliers-routing.module';
import { SupplierListComponent } from './supplier-list/supplier-list.component';
import { SupplierFormComponent } from './supplier-form/supplier-form.component';
import { SupplierDetailsComponent } from './supplier-details/supplier-details.component';
import { SupplierPaymentListComponent } from './supplier-payments/supplier-payment-list/supplier-payment-list.component';
import { SupplierPaymentFormComponent } from './supplier-payments/supplier-payment-form/supplier-payment-form.component';
import { SupplierPaymentDetailsComponent } from './supplier-payments/supplier-payment-details/supplier-payment-details.component';
import { SupplierAgingComponent } from './supplier-aging/supplier-aging.component';
import { SupplierApReconciliationComponent } from './supplier-ap-reconciliation/supplier-ap-reconciliation.component';

@NgModule({
  declarations: [
    SupplierListComponent,
    SupplierFormComponent,
    SupplierDetailsComponent,
    SupplierPaymentListComponent,
    SupplierPaymentFormComponent,
    SupplierPaymentDetailsComponent,
    SupplierAgingComponent,
    SupplierApReconciliationComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    SuppliersRoutingModule
  ]
})
export class SuppliersModule { }
