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
      { path: 'orders', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_VIEW'], mode: 'list' } },
      { path: 'orders/create', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_CREATE'], mode: 'create' } },
      { path: 'orders/edit/:id', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_EDIT'], mode: 'edit' } },
      { path: 'orders/details/:id', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_ORDER_VIEW'], mode: 'details' } },
      { path: 'receives', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RECEIVE_VIEW'], mode: 'list' } },
      { path: 'receives/create/:orderId', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RECEIVE_CREATE'], mode: 'create' } },
      { path: 'receives/details/:id', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RECEIVE_VIEW'], mode: 'details' } },
      { path: 'invoices', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_INVOICE_VIEW'], mode: 'invoice-list' } },
      { path: 'invoices/create', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_INVOICE_CREATE'], mode: 'invoice-create' } },
      { path: 'invoices/details/:id', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_INVOICE_VIEW'], mode: 'invoice-details' } },
      { path: 'returns', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RETURN_VIEW'], mode: 'list' } },
      { path: 'returns/create', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RETURN_CREATE'], mode: 'create' } },
      { path: 'returns/details/:id', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_RETURN_VIEW'], mode: 'details' } },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PurchasesRoutingModule { }
