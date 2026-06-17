import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CustomerAgingComponent } from './customer-aging/customer-aging.component';
import { CustomerDetailsComponent } from './customer-details/customer-details.component';
import { CustomerFormComponent } from './customer-form/customer-form.component';
import { CustomerListComponent } from './customer-list/customer-list.component';
import { CustomerReceiptDetailsComponent } from './customer-receipts/customer-receipt-details/customer-receipt-details.component';
import { CustomerReceiptFormComponent } from './customer-receipts/customer-receipt-form/customer-receipt-form.component';
import { CustomerReceiptListComponent } from './customer-receipts/customer-receipt-list/customer-receipt-list.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'list', component: CustomerListComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_VIEW'] } },
      { path: 'aging', component: CustomerAgingComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_AGING_VIEW'] } },
      { path: 'create', component: CustomerFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_CREATE'] } },
      { path: 'edit/:id', component: CustomerFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_EDIT'] } },
      { path: 'details/:id', component: CustomerDetailsComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_VIEW', 'CUSTOMER_LEDGER_VIEW'] } },
      { path: 'receipts', component: CustomerReceiptListComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_VIEW'] } },
      { path: 'receipts/create', component: CustomerReceiptFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_CREATE'] } },
      { path: 'receipts/edit/:id', component: CustomerReceiptFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_EDIT'] } },
      { path: 'receipts/details/:id', component: CustomerReceiptDetailsComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_VIEW'] } },
      { path: 'customer-list', redirectTo: 'list', pathMatch: 'full' },
      { path: 'customer-form', redirectTo: 'create', pathMatch: 'full' },
      { path: '', redirectTo: 'list', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CustomersRoutingModule { }
