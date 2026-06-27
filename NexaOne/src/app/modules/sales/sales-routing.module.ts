import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InvoiceFormComponent } from './invoice-form/invoice-form.component';
import { InvoiceListComponent } from './invoice-list/invoice-list.component';
import { OrdersComponent } from './orders/orders.component';
import { ReturnsComponent } from './returns/returns.component';
import { PosComponent } from './pos/pos.component';
import { SaleItemsComponent } from './sale-items/sale-items.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', component: InvoiceListComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_VIEW'], breadcrumb: 'All Sales' } },
      { path: 'add', component: InvoiceFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_CREATE'], mode: 'create', breadcrumb: 'Add Sale' } },
      { path: 'pos', component: PosComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_CREATE'], breadcrumb: 'Point of Sale' } },
      { path: 'items', component: SaleItemsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_VIEW'], breadcrumb: 'Sale Items' } },
      { path: 'orders', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_VIEW'], mode: 'list', breadcrumb: 'Sales Orders' } },
      { path: 'orders/create', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_CREATE'], mode: 'create', breadcrumb: 'Create' } },
      { path: 'orders/edit/:id', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_EDIT'], mode: 'edit', breadcrumb: 'Edit' } },
      { path: 'orders/details/:id', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_ORDER_VIEW'], mode: 'details', breadcrumb: 'Details' } },
      { path: 'invoices', redirectTo: '', pathMatch: 'full' },
      { path: 'invoices/create', redirectTo: 'add', pathMatch: 'full' },
      { path: 'invoices/edit/:id', redirectTo: ':id/edit', pathMatch: 'full' },
      { path: 'invoices/details/:id', redirectTo: ':id/view', pathMatch: 'full' },
      { path: 'returns', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_VIEW'], mode: 'list', breadcrumb: 'Sales Returns' } },
      { path: 'returns/create', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_CREATE'], mode: 'create', breadcrumb: 'Create' } },
      { path: 'returns/edit/:id', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_EDIT'], mode: 'edit', breadcrumb: 'Edit' } },
      { path: 'returns/details/:id', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_RETURN_VIEW'], mode: 'details', breadcrumb: 'Details' } },
      { path: ':id/view', component: InvoiceFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_VIEW'], mode: 'details', breadcrumb: 'Sale Details' } },
      { path: ':id/edit', component: InvoiceFormComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_INVOICE_EDIT'], mode: 'edit', breadcrumb: 'Edit Sale' } }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SalesRoutingModule { }
