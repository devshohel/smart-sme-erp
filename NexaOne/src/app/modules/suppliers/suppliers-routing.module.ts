import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SupplierFormComponent } from './supplier-form/supplier-form.component';
import { SupplierListComponent } from './supplier-list/supplier-list.component';
import { PermissionGuard } from '../auth/permission.guard';
import { SupplierDetailsComponent } from './supplier-details/supplier-details.component';
import { SupplierPaymentListComponent } from './supplier-payments/supplier-payment-list/supplier-payment-list.component';
import { SupplierPaymentFormComponent } from './supplier-payments/supplier-payment-form/supplier-payment-form.component';
import { SupplierPaymentDetailsComponent } from './supplier-payments/supplier-payment-details/supplier-payment-details.component';
import { SupplierAgingComponent } from './supplier-aging/supplier-aging.component';
import { SupplierApReconciliationComponent } from './supplier-ap-reconciliation/supplier-ap-reconciliation.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'list', component: SupplierListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['SUPPLIER_VIEW', 'SUPPLIER_LEDGER_VIEW', 'SUPPLIER_EDIT'] } },
      { path: 'details/:id', component: SupplierDetailsComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['SUPPLIER_VIEW', 'SUPPLIER_LEDGER_VIEW'] } },
      { path: 'create', component: SupplierFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_CREATE'] } },
      { path: 'edit/:id', component: SupplierFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_EDIT'] } },
      { path: 'payments', component: SupplierPaymentListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['SUPPLIER_PAYMENT_VIEW', 'SUPPLIER_PAYMENT_CREATE', 'SUPPLIER_PAYMENT_EDIT'] } },
      { path: 'payments/create', component: SupplierPaymentFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_PAYMENT_CREATE'] } },
      { path: 'payments/details/:id', component: SupplierPaymentDetailsComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_PAYMENT_VIEW'] } },
      { path: 'payments/edit/:id', component: SupplierPaymentFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_PAYMENT_EDIT'] } },
      { path: 'aging', component: SupplierAgingComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['SUPPLIER_LEDGER_VIEW', 'SUPPLIER_VIEW'] } },
      { path: 'ap-reconciliation', component: SupplierApReconciliationComponent, canActivate: [PermissionGuard], data: { permissions: ['SUPPLIER_LEDGER_VIEW'] } },
      { path: '', redirectTo: 'list', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SuppliersRoutingModule { }
