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
      { path: 'orders', component: PurchaseOrderComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_VIEW'] } },
      { path: 'invoices', component: PurchaseInvoiceComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_VIEW'] } },
      { path: 'returns', component: PurchaseReturnComponent, canActivate: [PermissionGuard], data: { permissions: ['PURCHASE_VIEW'] } },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PurchasesRoutingModule { }
