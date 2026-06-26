import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InvoicesComponent } from './invoices/invoices.component';
import { OrdersComponent } from './orders/orders.component';
import { ReturnsComponent } from './returns/returns.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'orders', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_VIEW'], mode: 'list', breadcrumb: 'Sales Orders' } },
      { path: 'orders/create', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_CREATE'], mode: 'create', breadcrumb: 'Create' } },
      { path: 'orders/edit/:id', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_EDIT'], mode: 'edit', breadcrumb: 'Edit' } },
      { path: 'orders/details/:id', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_VIEW'], mode: 'details', breadcrumb: 'Details' } },
      { path: 'invoices', component: InvoicesComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_VIEW'], mode: 'list', breadcrumb: 'Sales Invoice' } },
      { path: 'invoices/create', component: InvoicesComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_CREATE'], mode: 'create', breadcrumb: 'Create' } },
      { path: 'invoices/edit/:id', component: InvoicesComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_EDIT'], mode: 'edit', breadcrumb: 'Edit' } },
      { path: 'invoices/details/:id', component: InvoicesComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_VIEW'], mode: 'details', breadcrumb: 'Details' } },
      { path: 'returns', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_VIEW'], mode: 'list', breadcrumb: 'Sales Returns' } },
      { path: 'returns/create', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_CREATE'], mode: 'create', breadcrumb: 'Create' } },
      { path: 'returns/edit/:id', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_EDIT'], mode: 'edit', breadcrumb: 'Edit' } },
      { path: 'returns/details/:id', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_VIEW'], mode: 'details', breadcrumb: 'Details' } },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SalesRoutingModule { }
