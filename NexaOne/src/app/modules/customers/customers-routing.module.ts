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
      { path: 'list', component: CustomerListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_VIEW', 'CUSTOMER_LEDGER_VIEW', 'CUSTOMER_EDIT'], breadcrumb: 'Customer List' } },
      { path: 'aging', component: CustomerAgingComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_AGING_VIEW'], breadcrumb: 'Customer Aging' } },
      { path: 'dues', component: CustomerAgingComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_AGING_VIEW', 'CUSTOMER_RECEIPT_CREATE'], mode: 'dues', breadcrumb: 'Customer Dues' } },
      { path: 'create', component: CustomerFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_CREATE'], breadcrumb: 'Create Customer' } },
      { path: 'edit/:id', component: CustomerFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_EDIT'], breadcrumb: 'Edit Customer' } },
      { path: 'details/:id', component: CustomerDetailsComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_VIEW', 'CUSTOMER_LEDGER_VIEW'], breadcrumb: 'Details' } },
      { path: 'receipts', component: CustomerReceiptListComponent, canActivate: [PermissionGuard], data: { anyPermissions: ['CUSTOMER_RECEIPT_VIEW', 'CUSTOMER_RECEIPT_CREATE', 'CUSTOMER_RECEIPT_EDIT'], breadcrumb: 'Customer Receipts' } },
      { path: 'receipts/create', component: CustomerReceiptFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_CREATE'], breadcrumb: 'Create Receipt' } },
      { path: 'receipts/edit/:id', component: CustomerReceiptFormComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_EDIT'], breadcrumb: 'Edit Receipt' } },
      { path: 'receipts/details/:id', component: CustomerReceiptDetailsComponent, canActivate: [PermissionGuard], data: { permissions: ['CUSTOMER_RECEIPT_VIEW'], breadcrumb: 'Receipt Details' } },
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
