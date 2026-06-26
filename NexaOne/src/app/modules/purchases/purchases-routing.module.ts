import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PurchaseInvoiceComponent } from './purchase-invoice/purchase-invoice.component';
import { PurchaseOrderComponent } from './purchase-order/purchase-order.component';
import { PurchaseReturnComponent } from './purchase-return/purchase-return.component';
import { PermissionGuard } from '../auth/permission.guard';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: 'orders', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_VIEW'], mode: 'list', breadcrumb: 'Purchase Orders' } },
      { path: 'orders/create', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_CREATE'], mode: 'create', breadcrumb: 'Create' } },
      { path: 'orders/edit/:id', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_EDIT'], mode: 'edit', breadcrumb: 'Edit' } },
      { path: 'orders/details/:id', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_VIEW'], mode: 'details', breadcrumb: 'Details' } },
      { path: 'receives', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RECEIVE_VIEW'], mode: 'list', breadcrumb: 'Goods Receives' } },
      { path: 'receives/create/:orderId', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RECEIVE_CREATE'], mode: 'create', breadcrumb: 'Create Receive' } },
      { path: 'receives/details/:id', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RECEIVE_VIEW'], mode: 'details', breadcrumb: 'Receive Details' } },
      { path: 'invoices', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_INVOICE_VIEW'], mode: 'invoice-list', breadcrumb: 'Purchase Invoices' } },
      { path: 'invoices/create', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_INVOICE_CREATE'], mode: 'invoice-create', breadcrumb: 'Create Invoice' } },
      { path: 'invoices/details/:id', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_INVOICE_VIEW'], mode: 'invoice-details', breadcrumb: 'Invoice Details' } },
      { path: 'returns', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RETURN_VIEW'], mode: 'list', breadcrumb: 'Purchase Returns' } },
      { path: 'returns/create', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RETURN_CREATE'], mode: 'create', breadcrumb: 'Create Return' } },
      { path: 'returns/details/:id', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RETURN_VIEW'], mode: 'details', breadcrumb: 'Return Details' } },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PurchasesRoutingModule { }
