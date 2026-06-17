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
      { path: 'orders', component: OrdersComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_VIEW'] } },
      { path: 'invoices', component: InvoicesComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_VIEW'] } },
      { path: 'returns', component: ReturnsComponent, canActivate: [PermissionGuard], data: { permissions: ['SALES_VIEW'] } },
      { path: '', redirectTo: 'orders', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SalesRoutingModule { }
